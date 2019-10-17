package cn.hengyumo.humor.wx.utils;


import cn.hengyumo.humor.base.utils.HttpClientUtil;
import cn.hengyumo.humor.wx.utils.config.WxApp;
import cn.hengyumo.humor.wx.utils.config.WxRequestUrls;
import cn.hengyumo.humor.wx.utils.dto.WxLoginDto;
import cn.hengyumo.humor.wx.utils.exception.WxRequestException;
import cn.hengyumo.humor.wx.utils.exception.WxRequestResultEnum;
import cn.hengyumo.humor.wx.utils.vo.WxSessionVo;
import com.alibaba.fastjson.JSONObject;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.IOException;
import java.net.URISyntaxException;

/**
 * WxUtils
 *
 * @author hengyumo
 * @version 1.0
 * @since 2019/9/14
 */
@Component
public class WxUtils implements WxRequestUrls {

    @Resource
    private WxApp wxApp;

    /**
     *  微信登录 https://developers.weixin.qq.com/miniprogram/dev/api-backend/open-api/login/auth.code2Session.html
     */
    public WxSessionVo login(String code) {
        WxLoginDto wxLoginDto = new WxLoginDto();
        wxLoginDto.setAppid(wxApp.getAppId());
        wxLoginDto.setSecret(wxApp.getAppSecret());
        wxLoginDto.setJs_code(code);
        String jsonString;
        try {
            jsonString = HttpClientUtil.get(LOGIN, wxLoginDto);
        } catch (URISyntaxException e) {
            throw new WxRequestException(WxRequestResultEnum.REQUEST_URI_INVALID);
        } catch (IOException e) {
            throw new WxRequestException(WxRequestResultEnum.NET_CONNECT_FAIL);
        }
        JSONObject jsonObject =  JSONObject.parseObject(jsonString);
        Integer errCode = (Integer) jsonObject.get("errcode");
        if (errCode != null) {
            WxRequestResultEnum wxRequestResultEnum = WxRequestResultEnum.getByCode(errCode);
            if (wxRequestResultEnum == null) {
                throw new WxRequestException(WxRequestResultEnum.WX_REQUEST_FAIL);
            }
            if (! wxRequestResultEnum.equals(WxRequestResultEnum.WX_REQUEST_SUCCEED)) {
                throw new WxRequestException(wxRequestResultEnum);
            }
        }
        return JSONObject.toJavaObject(jsonObject, WxSessionVo.class);
    }
}
