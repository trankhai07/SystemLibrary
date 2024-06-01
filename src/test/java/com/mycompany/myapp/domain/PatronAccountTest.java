package com.mycompany.myapp.domain;

import static org.assertj.core.api.Assertions.assertThat;

import com.mycompany.myapp.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class PatronAccountTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(PatronAccount.class);
        PatronAccount patronAccount1 = new PatronAccount();
        patronAccount1.setCardNumber("id1");
        PatronAccount patronAccount2 = new PatronAccount();
        patronAccount2.setCardNumber(patronAccount1.getCardNumber());
        assertThat(patronAccount1).isEqualTo(patronAccount2);
        patronAccount2.setCardNumber("id2");
        assertThat(patronAccount1).isNotEqualTo(patronAccount2);
        patronAccount1.setCardNumber(null);
        assertThat(patronAccount1).isNotEqualTo(patronAccount2);
    }
}
