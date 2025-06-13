package com.xkball.xorlib.common.data;

import com.xkball.xorlib.api.annotation.ModMeta;

import java.util.List;

public record APVersionData(String loader, List<String> versions, ModMeta.Feature feature) {
}
