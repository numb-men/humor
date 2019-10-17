package cn.hengyumo.humor.wx.user;


import cn.hengyumo.humor.base.cache.EhcacheCache;
import cn.hengyumo.humor.base.mvc.BaseService;
import cn.hengyumo.humor.base.utils.BaseUtil;
import cn.hengyumo.humor.base.utils.TokenUtil;
import cn.hengyumo.humor.wx.utils.WxUtils;
import cn.hengyumo.humor.wx.utils.vo.WxSessionVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * WxUserService
 *
 * @author hengyumo
 * @version 1.0
 * @since 2019/9/14
 */
@Slf4j
@Service
public class WxUserService extends BaseService<WxUser, Long> {

    @Resource
    private WxUtils wxUtils;

    @Resource
    private BaseUtil baseUtil;

    @Resource
    private WxUserDao wxUserDao;


    public String login(String code) {
        WxSessionVo wxSessionVo = wxUtils.login(code);
        String key = "wx_" + wxSessionVo.getOpenid();
        EhcacheCache.setValue("wxSessionCache", key , wxSessionVo);
        return TokenUtil.createToken(key, wxSessionVo.getSession_key());
    }

    public void saveInfo(WxUserInfoDto wxUserInfoDto) {
        WxUser wxUser = null;
        String openId = "";
        WxCurrentUser wxCurrentUser = null;
        if (baseUtil.currentUserIsWxUser()) {
            wxCurrentUser = baseUtil.getWxCurrentUser();
            if (wxCurrentUser.getId() == null) {
                openId = wxCurrentUser.getWxOpenId();
            }
            else {
                openId = wxCurrentUser.getWxOpenId();
                wxUser = wxCurrentUser.getWxUser();
            }
        }
        else if (wxUserInfoDto.getOpenId() != null) {
            wxUser = wxUserDao.findByOpenId(wxUserInfoDto.getOpenId());
            if (wxUser == null) {
                openId = wxUserInfoDto.getOpenId();
            }
        }
        else {
            throw new WxUserException(WxUserResultEnum.SAVE_USER_INFO_FAIL);
        }
        if (wxUser == null) {
            wxUser = new WxUser();
        }
        BeanUtils.copyProperties(wxUserInfoDto, wxUser);
        wxUser.setOpenId(openId);
        WxUser wxUserSaved = save(wxUser);
    }
}
