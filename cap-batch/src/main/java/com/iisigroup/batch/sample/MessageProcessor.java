package com.iisigroup.batch.sample;

import org.springframework.batch.item.ItemProcessor;

/**
 * Spring Batch ItemProcessor for messageJob.
 * Receives each item from messageReader and optionally transforms it before passing to messageWriter.
 */
public class MessageProcessor implements ItemProcessor<Object, Object> {

    @Override
    public Object process(Object item) throws Exception {
        // TODO: 加入處理邏輯
        return item;
    }
}
