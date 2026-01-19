package com.Passman.Manager.Auth.POJO;


import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class KdfParams {
    private String algorithm;
    private int memory;
    private int iterations;
    private int parallelism;
}
