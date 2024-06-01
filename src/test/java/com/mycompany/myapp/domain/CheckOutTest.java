package com.mycompany.myapp.domain;

import static org.assertj.core.api.Assertions.assertThat;

import com.mycompany.myapp.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class CheckOutTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(CheckOut.class);
        CheckOut checkOut1 = new CheckOut();
        checkOut1.setId(1L);
        CheckOut checkOut2 = new CheckOut();
        checkOut2.setId(checkOut1.getId());
        assertThat(checkOut1).isEqualTo(checkOut2);
        checkOut2.setId(2L);
        assertThat(checkOut1).isNotEqualTo(checkOut2);
        checkOut1.setId(null);
        assertThat(checkOut1).isNotEqualTo(checkOut2);
    }
}
