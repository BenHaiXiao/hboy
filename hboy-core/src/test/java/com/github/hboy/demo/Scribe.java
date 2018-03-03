/*
 * Copyright (C) 2012 Facebook, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License. You may obtain
 * a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package com.github.hboy.demo;

import java.util.List;

import com.facebook.swift.codec.ThriftDocumentation;
import com.facebook.swift.service.*;

@ThriftService
public interface Scribe
{   
    @ThriftDocumentation("test")
    @ThriftMethod()
    ResultCode log(List<LogEntry> messages);

    
    @ThriftMethod("testString")
    String testString(String str);
 
    
    @ThriftMethod("testVoid1")
    void testVoid();
    
    
    @ThriftMethod("testObj")
    LogEntry testObj(LogEntry messages);
}
