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

import com.facebook.swift.codec.ThriftConstructor;
import com.facebook.swift.codec.ThriftField;
import com.facebook.swift.codec.ThriftStruct;

@ThriftStruct
public final class LogEntry
{
    private final String category;

    @ThriftConstructor
    public LogEntry(
            @ThriftField(name = "category") String category)
    {
        this.category = category;
    }

    @ThriftField(1)
    public String getCategory()
    {
        return category;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        final LogEntry logEntry = (LogEntry) o;

        if (category != null ? !category.equals(logEntry.category) : logEntry.category != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode()
    {
        int result = category != null ? category.hashCode() : 0;
        result = 31 * result ;
        return result;
    }

    @Override
    public String toString()
    {
        final StringBuilder sb = new StringBuilder();
        sb.append("LogEntryStruct");
        sb.append("{category='").append(category).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
