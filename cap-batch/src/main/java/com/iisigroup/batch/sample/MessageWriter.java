package com.iisigroup.batch.sample;

import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;

/**
 * Spring Batch ItemWriter for messageJob.
 * Receives each processed chunk from messageProcessor and writes it to the target destination.
 */
public class MessageWriter implements ItemWriter<Object> {

    @Override
    public void write(Chunk<?> chunk) throws Exception {
        // TODO: 加入寫出邏輯
    }
}
