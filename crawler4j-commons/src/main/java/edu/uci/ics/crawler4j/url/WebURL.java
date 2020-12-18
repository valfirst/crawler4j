package edu.uci.ics.crawler4j.url;

import java.io.Serializable;
import java.util.Map;

public interface WebURL extends Serializable {

    /**
     * Set the TLDList if you want {@linkplain #getDomain()} and
     * {@link #getSubDomain()} to properly identify effective top level registeredDomain as
     * defined at <a href="https://publicsuffix.org">publicsuffix.org</a>
     */
    void setTldList(TLDList tldList);

    /**
     * @return unique document id assigned to this Url.
     */
    int getDocid();

    void setDocid(int docid);

    /**
     * @return Url string
     */
    String getURL();

    void setURL(String url);

    /**
     * @return unique document id of the parent page. The parent page is the
     * page in which the Url of this page is first observed.
     */
    int getParentDocid();

    void setParentDocid(int parentDocid);

    /**
     * @return url of the parent page. The parent page is the page in which
     * the Url of this page is first observed.
     */
    String getParentUrl();

    void setParentUrl(String parentUrl);

    /**
     * @return crawl depth at which this Url is first observed. Seed Urls
     * are at depth 0. Urls that are extracted from seed Urls are at depth 1, etc.
     */
    short getDepth()
    ;

    void setDepth(short depth);

    /**
     * If {@link WebURLImpl} was provided with a {@link TLDList} then domain will be the
     * privately registered domain which is an immediate child of an effective top
     * level domain as defined at
     * <a href="https://publicsuffix.org">publicsuffix.org</a>. Otherwise it will be
     * the entire domain.
     *
     * @return Domain of this Url. For 'http://www.example.com/sample.htm',
     * effective top level domain is 'example.com'. For
     * 'http://www.my.company.co.uk' the domain is 'company.co.uk'.
     */
    String getDomain();

    /**
     * If {@link WebURLImpl} was provided with a {@link TLDList} then subDomain will be
     * the private portion of the entire domain which is a child of the identified
     * registered domain. Otherwise it will be empty. e.g. in
     * "http://www.example.com" the subdomain is "www". In
     * "http://www.my.company.co.uk" the subdomain would be "www.my".
     */
    String getSubDomain();

    /**
     * @return path of this Url. For 'http://www.example.com/sample.htm', registeredDomain will be 'sample.htm'
     */
    String getPath();

    void setPath(String path);

    /**
     * @return anchor string. For example, in <a href="example.com">A sample anchor</a>
     * the anchor string is 'A sample anchor'
     */
    String getAnchor();

    void setAnchor(String anchor);

    /**
     * @return priority for crawling this URL. A lower number results in higher priority.
     */
    byte getPriority();

    void setPriority(byte priority);

    /**
     * @return tag in which this URL is found
     */
    String getTag();

    void setTag(String tag);

    Map<String, String> getAttributes();

    void setAttributes(Map<String, String> attributes);

    String getAttribute(String name);

}
