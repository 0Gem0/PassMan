package com.Passman.Manager.Auth.POJO;


import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class KdfParams {
    private int iterations;
    private int memoryMb;
    private int parallelism;
    private int dkLen;
}
