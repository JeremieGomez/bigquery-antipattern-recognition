/*
 * Copyright 2023 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.zetasql.toolkit.antipattern.cmd;

import com.google.cloud.bigquery.FieldValueList;
import com.google.cloud.bigquery.TableResult;
import com.google.zetasql.toolkit.antipattern.util.BigQueryHelper;
import java.util.Iterator;

public class InformationSchemaQueryIterable implements Iterator<InputQuery> {

  Iterator<FieldValueList> fieldValueListIterator;
  String IS_TABLE_DEFAULT = "`region-us`.INFORMATION_SCHEMA.JOBS";
  String DAYS_BACK_DEFAULT = "30";
  Integer SLOTMS_MIN_DEFAULT = 0;


  public InformationSchemaQueryIterable(String projectId, String customDaysBack, String customISTable,
                                        String infoSchemaSlotmsMin)
          throws InterruptedException {

    String daysBack = customDaysBack == null ? DAYS_BACK_DEFAULT : customDaysBack;
    String ISTable = customISTable == null ? IS_TABLE_DEFAULT : customISTable;
    Integer slotsMsMin = infoSchemaSlotmsMin == null ? SLOTMS_MIN_DEFAULT : Integer.parseInt(infoSchemaSlotmsMin);

    TableResult tableResult =
        BigQueryHelper.getQueriesFromIS(projectId, daysBack, ISTable, slotsMsMin);
    fieldValueListIterator = tableResult.iterateAll().iterator();
  }

  @Override
  public boolean hasNext() {
    return fieldValueListIterator.hasNext();
  }

  @Override
  public InputQuery next() {
    FieldValueList row = fieldValueListIterator.next();
    String job_id = row.get("job_id").getStringValue();
    String query = row.get("query").getStringValue();
    String projectId = row.get("project_id").getStringValue();
    String userEmail = row.get("user_email").getStringValue();
    // Slot hours can be null if the query errors out
    String slot_hours = row.get("slot_hours").isNull() ? "0" : row.get("slot_hours").getStringValue();
    return new InputQuery(query, job_id, projectId, userEmail, Float.parseFloat(slot_hours));
  }
}
