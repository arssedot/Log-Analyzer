package com.arssedot.loganalyzer.service;

import com.arssedot.loganalyzer.domain.DashboardPage;
import com.arssedot.loganalyzer.domain.User;
import com.arssedot.loganalyzer.repository.DashboardPageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class DashboardPageService {

    private final DashboardPageRepository pageRepository;

    public record PageDto(Long id, String name, String serviceName, int position) {
        static PageDto from(DashboardPage p) {
            return new PageDto(p.getId(), p.getName(), p.getServiceName(), p.getPosition());
        }
    }

    public record PageRequest(String name, String serviceName) {}

    @Transactional(readOnly = true)
    public List<PageDto> getPages(User user) {
        List<DashboardPage> pages = pageRepository.findByUserIdOrderByPosition(user.getId());
        if (pages.isEmpty()) {
            return List.of(PageDto.from(createDefault(user)));
        }
        return pages.stream().map(PageDto::from).toList();
    }

    @Transactional
    public PageDto createPage(User user, PageRequest request) {
        if (request.name() == null || request.name().isBlank()) {
            throw new IllegalArgumentException("Page name must not be blank");
        }
        int nextPos = pageRepository.countByUserId(user.getId());
        DashboardPage page = DashboardPage.builder()
                .user(user)
                .name(request.name().strip())
                .serviceName(request.serviceName() != null && !request.serviceName().isBlank()
                        ? request.serviceName().strip() : null)
                .position(nextPos)
                .build();
        return PageDto.from(pageRepository.save(page));
    }

    @Transactional
    public PageDto updatePage(User user, Long pageId, PageRequest request) {
        DashboardPage page = requireOwned(user, pageId);
        if (request.name() != null && !request.name().isBlank()) {
            page.setName(request.name().strip());
        }
        if (request.serviceName() != null) {
            page.setServiceName(request.serviceName().isBlank() ? null : request.serviceName().strip());
        }
        return PageDto.from(pageRepository.save(page));
    }

    @Transactional
    public void deletePage(User user, Long pageId) {
        requireOwned(user, pageId);
        if (pageRepository.countByUserId(user.getId()) <= 1) {
            throw new IllegalStateException("Cannot delete the last page");
        }
        pageRepository.deleteById(pageId);
    }

    private DashboardPage createDefault(User user) {
        DashboardPage page = DashboardPage.builder()
                .user(user)
                .name("Default")
                .position(0)
                .build();
        return pageRepository.save(page);
    }

    private DashboardPage requireOwned(User user, Long pageId) {
        DashboardPage page = pageRepository.findById(pageId)
                .orElseThrow(() -> new NoSuchElementException("Page not found: " + pageId));
        if (!page.getUser().getId().equals(user.getId())) {
            throw new AccessDeniedException("Page does not belong to current user");
        }
        return page;
    }
}
