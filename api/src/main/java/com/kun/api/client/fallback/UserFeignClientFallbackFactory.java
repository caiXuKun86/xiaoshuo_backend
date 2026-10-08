package com.kun.api.client.fallback;

import com.kun.api.client.UserFeignClient;
import com.kun.api.dto.user.RegisterWriterDTO;
import com.kun.api.dto.user.UserDTO;
import com.kun.api.dto.user.UserPointsUpdateDTO;
import com.kun.common.core.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;

@Slf4j
@Component
public class UserFeignClientFallbackFactory implements FallbackFactory<UserFeignClient> {

    @Override
    public UserFeignClient create(Throwable cause) {
        return new UserFeignClient() {
            @Override
            public Result<UserDTO> getUserById(Long userId) {
                log.error("调用 service-user 失败，触发降级，原因: {}", cause.getMessage());
                // 返回兜底的默认值或友好提示
                return Result.fail(503, "用户服务不可用(降级响应)");
            }

            @Override
            public Result<List<UserDTO>> getUserByIds(Collection<Long> userIds) {
                log.error("调用 service-user 失败，触发降级，原因: {}", cause.getMessage());
                return Result.fail(503, "用户服务不可用(降级响应)");
            }

            @Override
            public Result<Boolean> updatePoints(UserPointsUpdateDTO updateDTO) {
                log.error("调用 service-user 失败，触发降级，原因: {}", cause.getMessage());
                return Result.fail(503, "用户服务不可用(降级响应)");
            }

            @Override
            public Result<Boolean> registerWriter(RegisterWriterDTO registerWriterDTO) {
                log.error("调用 service-user 失败，触发降级，原因: {}", cause.getMessage());
                return Result.fail(503, "用户服务不可用(降级响应)");
            }
        };
    }
}