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

/**
 * A class to encapulate a command to the ECU to inject into the flow over the serial link
 * @author dgs
 *
 */
public class InjectedCommand
{
    private byte[] command;
    private boolean returnResult;
    private int resultId;
    private int delay;

    /**
     * 
     * @param command       The command to send to the ECU
     * @param delay         ms to wait after sending the command before attempting any read
     * @param returnResult  true if we expect the ECU to reply
     * @param resultId      An ID to apply to the result
     */
    public InjectedCommand(byte[] command, int delay, boolean returnResult, int resultId)
    {
        this.command = command;
        this.delay = delay;
        this.returnResult = returnResult;
        this.resultId = resultId;
    }

    public byte[] getCommand()
    {
        return command;
    }

    public boolean isReturnResult()
    {
        return returnResult;
    }

    public int getResultId()
    {
        return resultId;
    }

    public int getDelay()
    {
        return delay;
    }
}
