package edu.uci.ics.crawler4j.frontier;

import com.sleepycat.bind.tuple.TupleBinding;
import com.sleepycat.bind.tuple.TupleInput;
import com.sleepycat.bind.tuple.TupleOutput;

import edu.uci.ics.crawler4j.url.WebURL;
import edu.uci.ics.crawler4j.url.WebURLImpl;

/**
 * @author Yasser Ganjisaffar
 */
public class WebURLTupleBinding extends TupleBinding<WebURL> {

    @Override
    public WebURL entryToObject(TupleInput input) {
        WebURLImpl webURL = new WebURLImpl();
        webURL.setURL(input.readString());
        webURL.setDocid(input.readInt());
        webURL.setParentDocid(input.readInt());
        webURL.setParentUrl(input.readString());
        webURL.setDepth(input.readShort());
        webURL.setPriority(input.readByte());
        webURL.setAnchor(input.readString());
        return webURL;
    }

    @Override
    public void objectToEntry(WebURL url, TupleOutput output) {
        output.writeString(url.getURL());
        output.writeInt(url.getDocid());
        output.writeInt(url.getParentDocid());
        output.writeString(url.getParentUrl());
        output.writeShort(url.getDepth());
        output.writeByte(url.getPriority());
        output.writeString(url.getAnchor());
    }
}