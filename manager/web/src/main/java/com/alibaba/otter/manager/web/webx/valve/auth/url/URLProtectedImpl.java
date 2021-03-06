package com.alibaba.otter.manager.web.webx.valve.auth.url;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.oro.text.regex.PatternMatcher;
import org.apache.oro.text.regex.Perl5Matcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 基于url匹配的实现
 * 
 * @author jianghang 2011-11-11 下午12:41:30
 * @version 4.0.0
 */
public class URLProtectedImpl implements URLProtected {

    private static final Logger    logger           = LoggerFactory.getLogger(URLProtectedImpl.class);
    private List<URLPatternHolder> urlProtectedList = new ArrayList<URLPatternHolder>();

    public URLProtectedImpl(){
    }

    public URLProtectedImpl(List<URLPatternHolder> urlProtectedList){
        this.urlProtectedList = urlProtectedList;
    }

    public boolean check(String requestUrl) {
        if (StringUtils.isBlank(requestUrl)) {
            return false;
        }

        if (logger.isDebugEnabled()) {
            logger.debug("Converted URL to lowercase, from: '" + requestUrl + "'; to: '" + requestUrl + "'");
        }

        PatternMatcher matcher = new Perl5Matcher();

        Iterator<URLPatternHolder> iter = urlProtectedList.iterator();

        while (iter.hasNext()) {
            URLPatternHolder holder = (URLPatternHolder) iter.next();

            if (matcher.matches(requestUrl, holder.getCompiledPattern())) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Candidate is: '" + requestUrl + "'; pattern is "
                                 + holder.getCompiledPattern().getPattern() + "; matched=true");
                }
                return true;
            }
        }
        return false;
    }

    public List<URLPatternHolder> getUrlProtectedList() {
        return urlProtectedList;
    }

    public void setUrlProtectedList(List<URLPatternHolder> urlProtectedList) {
        this.urlProtectedList = urlProtectedList;
    }

    public void addUrlProtected(URLPatternHolder urlProtected) {
        this.urlProtectedList.add(urlProtected);
    }

}
