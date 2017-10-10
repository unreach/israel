/*
 * Copyright (c) 2016, Quancheng-ec.com All right reserved. This software is the
 * confidential and proprietary information of Quancheng-ec.com ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with Quancheng-ec.com.
 */
package io.unreach;

import com.github.os72.protocjar.Protoc;
import com.google.protobuf.DescriptorProtos.FileDescriptorSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * @author shimingliu 2016年12月21日 下午3:06:47
 * @version CommondProtoc.java, v 0.0.1 2016年12月21日 下午3:06:47 shimingliu
 */
public class CommondProtoc {

    private static final Logger logger = LoggerFactory.getLogger(CommondProtoc.class);

    private final String discoveryRoot;

    private CommondProtoc(String discoveryRoot) {
        this.discoveryRoot = discoveryRoot;
    }

    public static CommondProtoc configProtoPath(String discoveryRoot) {
        return new CommondProtoc(discoveryRoot);
    }

    public FileDescriptorSet invoke(String protoPath) {
        Path descriptorPath;
        try {
            descriptorPath = Files.createTempFile("descriptor", ".pb.bin");

            List<String> protocArgs = new ArrayList<>();
            protocArgs.add("-I" + discoveryRoot);
            protocArgs.add("--descriptor_set_out="
                    + descriptorPath.toAbsolutePath().toString());
            protocArgs.add(protoPath);
//            ImmutableList<String> protocArgs = ImmutableList.<String> builder()//
//                                                            .add("-I" + discoveryRoot)//
//                                                            .add("--descriptor_set_out="
//                                                                 + descriptorPath.toAbsolutePath().toString())//
//                                                            .add(protoPath)//
//                                                            .build();

            int status;
            String[] protocLogLines;
            PrintStream stdoutBackup = System.out;
            try {
                ByteArrayOutputStream protocStdout = new ByteArrayOutputStream();
                System.setOut(new PrintStream(protocStdout));

                status = Protoc.runProtoc(protocArgs.toArray(new String[0]));
                protocLogLines = protocStdout.toString().split("\n");
            } catch (IOException | InterruptedException e) {
                throw new IllegalArgumentException("Unable to execute protoc binary", e);
            } finally {
                System.setOut(stdoutBackup);
            }
            if (status != 0) {
                logger.warn("Protoc invocation failed with status: " + status);
                for (String line : protocLogLines) {
                    logger.warn("[Protoc log] " + line);
                }

                throw new IllegalArgumentException(String.format("Got exit code [%d] from protoc with args [%s]",
                        status, protocArgs));
            }
            return FileDescriptorSet.parseFrom(Files.readAllBytes(descriptorPath));
        } catch (IOException e) {
            throw new IllegalArgumentException(e);
        }
    }

}
