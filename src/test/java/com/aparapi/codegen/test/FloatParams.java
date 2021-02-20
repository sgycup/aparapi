/**
 * Copyright (c) 2016 - 2018 Syncleus, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
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
package com.aparapi.codegen.test;

public class FloatParams {

    int addEmUp(float y, float z) {
        return ((int) y + (int) z);
    }

    public void run() {

        int y = 2;

        float x = 0f;

        addEmUp((x = (float) y), x);

    }
}
/**{OpenCL{
 typedef struct This_s{

 int passid;
 }This;
 int get_pass_id(This *this){
 return this->passid;
 }

 int com_amd_aparapi_test_FloatParams__addEmUp(This *this, float y, float z){
 return(((int)y + (int)z));
 }
 __kernel void run(
 int passid
 ){
 This thisStruct;
 This* this=&thisStruct;
 this->passid = passid;
 {
 int y = 2;
 float x = 0.0f;
 com_amd_aparapi_test_FloatParams__addEmUp(this, x=(float)y, x);
 return;
 }
 }
 }OpenCL}**/
