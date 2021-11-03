package com.company;

import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
public class SnowFlakeIdGenerator {
    // The initial time(2017-01-01)
    private static final long INITIAL_TIME_STAMP = 1483200000000L;

    // Machine ID bits
    private static final long WORK_ID_BITS = 5L;

    // DataCenter ID bits
    private static final long DATACENTER_ID_BITS = 5L;

    // The maximum machine ID supported is 31, which can quickly calculate the maximum decimal number that a few binary numbers can represent.
    private static final long MAX_WORKER_ID = ~(-1L << WORK_ID_BITS);

    // The maximum datacenter ID supported is 31
    private static final long MAX_DATACENTER_ID = ~(-1L << DATACENTER_ID_BITS);

    // Sequence ID bits
    private static final long SEQUENCE_BITS = 12L;

    // The machine ID offset,12
    private static final long WORKERID_OFFSET = SEQUENCE_BITS;

    // The datacent ID offset,12+5
    private static final long DATACENTERID_OFFSET = WORK_ID_BITS + SEQUENCE_BITS;

    // The timestape offset, 5+5+12
    private static final long TIMESTAMP_OFFSET = DATACENTER_ID_BITS + WORK_ID_BITS + SEQUENCE_BITS;

    // Sequence mask,4095
    private static final long SEQUENCE_MASK = ~(-1L << SEQUENCE_BITS);

    // Worker ID ,0~31
    private long workerId;

    // Datacenter ID,0~31
    private long datacenterId;

    // Sequence,0~4095
    private long sequence = 0L;

    // Last timestamp
    private long lastTimestamp = -1L;

    public SnowFlakeIdGenerator(long workerId, long datacenterId) {
        if (workerId > MAX_WORKER_ID || workerId < 0)
            throw new IllegalArgumentException(String.format("WorkerID can't be greater than %d or less than 0", MAX_WORKER_ID));
        if (datacenterId > MAX_DATACENTER_ID || datacenterId < 0)
            throw new IllegalArgumentException(String.format("DataCenterID can't be greater than %d or less than 0", MAX_WORKER_ID));
        this.workerId = workerId;
        this.datacenterId = datacenterId;
    }

    public synchronized long nextId() {
        long timeStamp = System.currentTimeMillis();
        if (timeStamp < lastTimestamp)
            throw new RuntimeException("The current time less than last time");
        if (timeStamp == lastTimestamp) {
            sequence = (sequence + 1) & SEQUENCE_MASK;
            if (0 == sequence)
                timeStamp = tillNextMillis(lastTimestamp);
        } else {
            sequence = 0L;
        }
        lastTimestamp = timeStamp;

        return (timeStamp - INITIAL_TIME_STAMP) << TIMESTAMP_OFFSET | (datacenterId << DATACENTERID_OFFSET) | (workerId << WORKERID_OFFSET) | sequence;
    }

    private long tillNextMillis(long lastTimestamp) {
        long timestamp = System.currentTimeMillis();
        while (timestamp <= lastTimestamp)
            timestamp = System.currentTimeMillis();
        return timestamp;
    }

    private static String to62(long id){
        StringBuffer id_62 = new StringBuffer();
        while(id>0){
            long i = id % 62;
            id = (id-i)/62;
            int ii = (int)i;
            id_62.insert(0,chs[ii]);
        }
        return id_62.toString();
    }
    private final static char[] chs={'a','b','c','d','e','f','g','h','i','j','k','l','m','n','o','p','q','r','s','t','u','v','w','x','y','z','A','B','C','D','E','F','G','H','I','J','K','L','M','N','O','P','Q','R','S','T','U','V','W','X','Y','Z','0','1','2','3','4','5','6','7','8','9'};

    public static void main(String[] args) {

        SnowFlakeIdGenerator generator = new SnowFlakeIdGenerator(1, 1);
        ExecutorService executorService = Executors.newCachedThreadPool();
        for (int i = 0; i < 5; i++) {
            executorService.execute(new Runnable() {
                @Override
                public void run() {
                    long id = generator.nextId();
                    System.out.println(id);
                    String id_62 = to62(id);
                    System.out.println(id_62);
                }
            });
        }
        executorService.shutdown();

        int[] a= {1,2,3};
        int b = a.length;
        Arrays.sort(a);
    }
}