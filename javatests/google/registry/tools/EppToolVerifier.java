// Copyright 2016 The Domain Registry Authors. All Rights Reserved.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package google.registry.tools;

import static com.google.common.collect.Iterables.pairUp;
import static com.google.common.truth.Truth.assertThat;
import static google.registry.util.ResourceUtils.readResourceUtf8;
import static google.registry.xml.XmlTestUtils.assertXmlEquals;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Arrays.asList;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.google.common.base.Pair;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableMap;
import com.google.common.net.MediaType;

import google.registry.tools.ServerSideCommand.Connection;

import org.mockito.ArgumentCaptor;

import java.net.URLDecoder;
import java.util.List;
import java.util.Map;

/** Class for verifying EPP commands sent to the server via the tool endpoint. */
public class EppToolVerifier {

  private final Connection connection;
  private final String clientIdentifier;
  private final boolean superuser;
  private final boolean dryRun;

  public EppToolVerifier() {
    this(null, null, false, false);
  }

  private EppToolVerifier(
      Connection connection, String clientIdentifier, boolean superuser, boolean dryRun) {
    this.connection = connection;
    this.clientIdentifier = clientIdentifier;
    this.superuser = superuser;
    this.dryRun = dryRun;
  }

  EppToolVerifier withConnection(Connection connection) {
    return new EppToolVerifier(connection, clientIdentifier, superuser, dryRun);
  }

  EppToolVerifier withClientIdentifier(String clientIdentifier) {
    return new EppToolVerifier(connection, clientIdentifier, superuser, dryRun);
  }

  EppToolVerifier asSuperuser() {
    return new EppToolVerifier(connection, clientIdentifier, true, dryRun);
  }

  EppToolVerifier asDryRun() {
    return new EppToolVerifier(connection, clientIdentifier, superuser, true);
  }

  void verifySent(String... xmlToMatch) throws Exception {
    ArgumentCaptor<byte[]> params = ArgumentCaptor.forClass(byte[].class);
    verify(connection, times(xmlToMatch.length)).send(
        eq("/_dr/epptool"),
        eq(ImmutableMap.<String, Object>of()),
        eq(MediaType.FORM_DATA),
        params.capture());
    List<byte[]> capturedParams = params.getAllValues();
    assertThat(capturedParams).hasSize(xmlToMatch.length);
    for (Pair<String, byte[]> xmlAndParams : pairUp(asList(xmlToMatch), capturedParams)) {
      Map<String, String> map = Splitter.on('&').withKeyValueSeparator('=')
          .split(new String(xmlAndParams.getSecond(), UTF_8));
      assertThat(map).hasSize(4);
      assertXmlEquals(
          readResourceUtf8(getClass(), "testdata/" + xmlAndParams.getFirst()),
          URLDecoder.decode(map.get("xml"), UTF_8.toString()));
      assertThat(map).containsEntry("dryRun", Boolean.toString(dryRun));
      assertThat(map).containsEntry("clientIdentifier", clientIdentifier);
      assertThat(map).containsEntry("superuser", Boolean.toString(superuser));
    }
  }
}