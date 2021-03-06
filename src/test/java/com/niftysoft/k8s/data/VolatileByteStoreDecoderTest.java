package com.niftysoft.k8s.data;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.embedded.EmbeddedChannel;
import org.junit.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

public class VolatileByteStoreDecoderTest {

  private static void writeEntry(ByteBuf buf, String key, String value, long version)
      throws Exception {
    buf.writeLong(VolatileByteStore.getHasher().apply(key));
    buf.writeLong(version);
    byte[] bytes = value.getBytes("UTF-8");
    buf.writeInt(bytes.length);
    buf.writeBytes(bytes);
  }

  @Test
  public void testDecodesEmptyMap() {
    EmbeddedChannel channel =
        new EmbeddedChannel(new VolatileByteStore.VolatileByteStoreDecoder());

    ByteBuf buf = Unpooled.buffer();
    buf.writeInt(0);

    assertThat(channel.writeInbound(buf)).isTrue();
    assertThat(channel.inboundMessages().size()).isEqualTo(1);

    Object obj = channel.readInbound();

    assertThat(obj).hasSameClassAs(new VolatileByteStore());
    assertThat(((VolatileByteStore) obj).isEmpty()).isTrue();
  }

  @Test
  public void testDecodesSingletonMap() throws Exception {
    EmbeddedChannel channel =
        new EmbeddedChannel(new VolatileByteStore.VolatileByteStoreDecoder());

    ByteBuf buf = Unpooled.buffer();
    buf.writeInt(1);
    writeEntry(buf, "key", "value", 12L);

    assertThat(channel.writeInbound(buf)).isTrue();
    assertThat(channel.inboundMessages().size()).isEqualTo(1);

    Object obj = channel.readInbound();

    assertThat(obj).hasSameClassAs(new VolatileByteStore());

    VolatileByteStore vss = (VolatileByteStore) obj;
    assertThat(vss.size()).isEqualTo(1);
    assertThat(vss.get("key")).isEqualTo("value".getBytes());
    assertThat(vss.getVersion("key")).isEqualTo(Optional.of(12L));
  }

  @Test
  public void testDecodesMultiKeyMap() throws Exception {
    EmbeddedChannel channel =
        new EmbeddedChannel(new VolatileByteStore.VolatileByteStoreDecoder());

    ByteBuf buf = Unpooled.buffer();
    buf.writeInt(5);
    writeEntry(buf, "alpha", "beta", 5L);
    writeEntry(buf, "gamma", "slamma", 17L);
    writeEntry(buf, "imma", "mamma", 1L);
    writeEntry(buf, "excelsior", "marvel", 0L);
    writeEntry(buf, "try it", "not really", 10L);

    assertThat(channel.writeInbound(buf)).isTrue();
    assertThat(channel.inboundMessages().size()).isEqualTo(1);

    Object obj = channel.readInbound();

    assertThat(obj).hasSameClassAs(new VolatileByteStore());

    VolatileByteStore vss = (VolatileByteStore) obj;

    assertThat(vss.size()).isEqualTo(5);
    assertThat(vss.get("alpha")).isEqualTo("beta".getBytes());
    assertThat(vss.get("gamma")).isEqualTo("slamma".getBytes());
    assertThat(vss.get("imma")).isEqualTo("mamma".getBytes());
    assertThat(vss.get("excelsior")).isEqualTo("marvel".getBytes());
    assertThat(vss.get("try it")).isEqualTo("not really".getBytes());

    assertThat(vss.getVersion("alpha")).isEqualTo(Optional.of(5L));
    assertThat(vss.getVersion("gamma")).isEqualTo(Optional.of(17L));
    assertThat(vss.getVersion("imma")).isEqualTo(Optional.of(1L));
    assertThat(vss.getVersion("excelsior")).isEqualTo(Optional.of(0L));
    assertThat(vss.getVersion("try it")).isEqualTo(Optional.of(10L));
  }

  // TODO: Fix this broken test
  @Test
  public void testDecodesUnicodeStrings() throws Exception {
    EmbeddedChannel channel =
        new EmbeddedChannel(new VolatileByteStore.VolatileByteStoreDecoder());

    String unicodeKey = "\u0020\u1234 \u1045";
    String unicodeValue = "\u2014 \u0134 \u1203";

    ByteBuf buf = Unpooled.buffer();
    buf.writeInt(1);
    writeEntry(buf, unicodeKey, unicodeValue, 0L);

    assertThat(channel.writeInbound(buf)).isTrue();
    assertThat(channel.inboundMessages().size()).isGreaterThanOrEqualTo(1);

    Object obj = channel.readInbound();

    assertThat(obj).hasSameClassAs(new VolatileByteStore());

    VolatileByteStore vss = (VolatileByteStore) obj;
    assertThat(vss.size()).isEqualTo(1);
    assertThat(vss.get(unicodeKey)).isEqualTo(unicodeValue.getBytes());
  }
}
