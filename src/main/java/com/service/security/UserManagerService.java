package com.service.security;

import com.domain.request.FullUserReq;
import com.domain.request.UserReq;
import com.domain.security.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Objects;

/**
 * Created by tang.cheng on 2017/1/14.
 */
@Service
public class UserManagerService {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserManagerService.class);
    @Autowired
    private UserAuthorityRepository userAuthorityRepository;

    @Autowired
    private UserRepository userRepository;


    /**
     * 第一个创建的一定是管理员
     *
     * @return
     */
    public boolean hasAdminAccount() {
        Long count = userAuthorityRepository.countBySysAuthorityId(SysRole.ROLE_ADMIN.getId());
        LOGGER.info("admin count:{}", count);
        return count > 0;
    }

    public void create(UserReq req, Number authorityId) {
        SysUser sysUser = new SysUser();
        sysUser.setUsername(req.getUsername());
        String encodePwd = req.getPassword();
        LOGGER.info("{}", encodePwd);
        sysUser.setPassword(encodePwd);
        sysUser.setFirstName(req.getFirstName());
        sysUser.setLastName(req.getLastName());
        sysUser.setEmail(req.getEmail());
        sysUser.setEnabled(true);
        sysUser.setLastPasswordResetDate(new Date());
        userRepository.save(sysUser);

        SysUserAuthority authority = new SysUserAuthority();
        authority.setSysUserId(sysUser.getId());
        authority.setSysAuthorityId((Long) authorityId);
        userAuthorityRepository.save(authority);
    }

    public void create(FullUserReq fullUserReq) {
        create(fullUserReq, fullUserReq.getRole());
    }

    public void manager(String username, Boolean status) {
        SysUser sysUser = userRepository.findByUsername(username);
        if (sysUser == null) {
            LOGGER.warn("{} not exists", username);
            return;
        }
        if (Objects.equals(status, sysUser.getEnabled())) {
            LOGGER.warn("{}, no changes.Nothing to do ", username);
            return;
        }
        sysUser.setEnabled(status);
        userRepository.saveAndFlush(sysUser);
    }
}
