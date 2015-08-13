package org.wildfly.extras.creaper.shrinkwrap;

import com.google.common.io.ByteSource;
import org.jboss.shrinkwrap.api.asset.Asset;

import java.io.IOException;
import java.io.InputStream;

/** An implementation of Guava's {@link ByteSource} for ShrinkWrap's {@link Asset}s. */
public final class AssetByteSource extends ByteSource {
    private final Asset asset;

    public AssetByteSource(Asset asset) {
        this.asset = asset;
    }

    @Override
    public InputStream openStream() throws IOException {
        return asset.openStream();
    }
}
