/**
 * Copyright 2011, 2012 David Smith.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this software except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package uk.org.smithfamily.mslogger.ecuDef;

public interface MSControllerInterface
{

    void registerOutputChannel(OutputChannel o);
    
    boolean isSet(String string);

    byte[] loadPage(int i, int j, int k, byte[] bs, byte[] bs2);

    int[] loadByteVector(byte[] pageBuffer, int offset, int width, boolean signed);

    int[][] loadByteArray(byte[] pageBuffer, int offset, int width, int height, boolean signed);

    int[] loadWordVector(byte[] pageBuffer, int offset, int width, boolean signed);

    int[][] loadWordArray(byte[] pageBuffer, int offset, int width, int height, boolean signed);

    double round(double x);

    int table(double x, String t);

    double timeNow();

    double tempCvt(double x);
}
