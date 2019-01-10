/*
 * Copyright 2015 Samsung Electronics Co., LTD
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package br.org.sidia.eva.service.data;

import br.org.sidia.eva.movement.EvaActionType;

public class EvaActionCommand implements Command {

    @EvaActionType
    private int type;

    public EvaActionCommand(@EvaActionType int type) {
        this.type = type;
    }

    @EvaActionType
    @Override
    public Integer getType() {
        return type;
    }

    @Override
    public String toString() {
        return "EvaActionCommand{" +
                "type=" + type +
                '}';
    }
}