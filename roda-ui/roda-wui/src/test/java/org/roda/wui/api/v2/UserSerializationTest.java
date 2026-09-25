/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.api.v2;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNull;

import org.roda.core.data.v2.user.User;
import org.testng.annotations.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

@Test(groups = "travis")
public class UserSerializationTest {
  private static final String[] TOKENS = {"reset-token", "reset-expiration", "email-token", "email-expiration"};

  private static User userWithTokens() {
    User user = new User("alice", "alice", false);
    user.setResetPasswordToken(TOKENS[0]);
    user.setResetPasswordTokenExpirationDate(TOKENS[1]);
    user.setEmailConfirmationToken(TOKENS[2]);
    user.setEmailConfirmationTokenExpirationDate(TOKENS[3]);
    return user;
  }

  @Test
  public void tokensAreNotSerialized() throws Exception {
    String json = new ObjectMapper().writeValueAsString(userWithTokens());

    for (String token : TOKENS) {
      assertFalse(json.contains(token), json);
    }
    assertFalse(json.contains("Token"), json);
  }

  @Test
  public void tokensAreNotDeserialized() throws Exception {
    String json = "{\"type\":\"User\",\"id\":\"alice\",\"name\":\"alice\",\"resetPasswordToken\":\"forged\","
      + "\"resetPasswordTokenExpirationDate\":\"forged\",\"emailConfirmationToken\":\"forged\","
      + "\"emailConfirmationTokenExpirationDate\":\"forged\"}";

    User user = new ObjectMapper().readValue(json, User.class);

    assertEquals(user.getId(), "alice");
    assertNull(user.getResetPasswordToken());
    assertNull(user.getResetPasswordTokenExpirationDate());
    assertNull(user.getEmailConfirmationToken());
    assertNull(user.getEmailConfirmationTokenExpirationDate());
  }

  @Test
  public void tokensAreNotInToString() {
    String text = userWithTokens().toString();

    for (String token : TOKENS) {
      assertFalse(text.contains(token), text);
    }
  }
}
