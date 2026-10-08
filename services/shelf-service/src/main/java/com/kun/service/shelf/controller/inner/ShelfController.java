package com.kun.service.shelf.controller.inner;

import com.kun.api.client.ShelfFeignClient;
import com.kun.api.dto.shelf.ShelfDTO;
import com.kun.common.core.result.Result;
import com.kun.service.shelf.service.BookshelfService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController("innerShelfController")
@RequiredArgsConstructor
public class ShelfController implements ShelfFeignClient {

    private final BookshelfService bookshelfService;



    @Override
    @GetMapping("/inner/shelf/info")
    public Result<ShelfDTO> getShelfDTO(Long bookId, Long userId) {
        ShelfDTO shelfDTO = bookshelfService.getShelfByBookId(bookId,userId);
        return Result.success(shelfDTO);


    }
}
