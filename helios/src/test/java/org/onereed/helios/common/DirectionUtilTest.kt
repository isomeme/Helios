package org.onereed.helios.common

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.onereed.helios.common.DirectionUtil.arc

class DirectionUtilTest {

  @Test
  fun arc() {
    assertThat(arc(0.0, 30.0)).isEqualTo(30.0)
    assertThat(arc(30.0, 0.0)).isEqualTo(-30.0)
    assertThat(arc(30.0, 30.0)).isEqualTo(0.0)
    assertThat(arc(30.0, -30.0)).isEqualTo(-60.0)
    assertThat(arc(-30.0, 30.0)).isEqualTo(60.0)

    assertThat(arc(-170.0, 170.0)).isEqualTo(-20.0)
    assertThat(arc(170.0, -170.0)).isEqualTo(20.0)
    assertThat(arc(10.0, 380.0)).isEqualTo(10.0)
    assertThat(arc(380.0, 10.0)).isEqualTo(-10.0)

    assertThat(arc(0.0, 180.0)).isEqualTo(180.0)
    assertThat(arc(180.0, 0.0)).isEqualTo(-180.0)
    assertThat(arc(0.0, -180.0)).isEqualTo(-180.0)
    assertThat(arc(-180.0, 0.0)).isEqualTo(180.0)

    assertThat(arc(0.0, 0.0)).isEqualTo(0.0)
    assertThat(arc(180.0, 180.0)).isEqualTo(0.0)
    assertThat(arc(740.0, 20.0)).isEqualTo(-0.0)
    assertThat(arc(20.0, 740.0)).isEqualTo(0.0)

    assertThat(arc(10.0, 200.0)).isEqualTo(-170.0)
    assertThat(arc(200.0, 10.0)).isEqualTo(170.0)
  }
}
