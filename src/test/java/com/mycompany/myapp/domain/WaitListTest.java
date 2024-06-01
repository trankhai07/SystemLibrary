package com.mycompany.myapp.domain;

import static org.assertj.core.api.Assertions.assertThat;

import com.mycompany.myapp.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class WaitListTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(WaitList.class);
        WaitList waitList1 = new WaitList();
        waitList1.setId(1L);
        WaitList waitList2 = new WaitList();
        waitList2.setId(waitList1.getId());
        assertThat(waitList1).isEqualTo(waitList2);
        waitList2.setId(2L);
        assertThat(waitList1).isNotEqualTo(waitList2);
        waitList1.setId(null);
        assertThat(waitList1).isNotEqualTo(waitList2);
    }
}
