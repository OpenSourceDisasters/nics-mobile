/*|~^~|Copyright (c) 2008-2016, Massachusetts Institute of Technology (MIT)
 |~^~|All rights reserved.
 |~^~|
 |~^~|Redistribution and use in source and binary forms, with or without
 |~^~|modification, are permitted provided that the following conditions are met:
 |~^~|
 |~^~|-1. Redistributions of source code must retain the above copyright notice, this
 |~^~|ist of conditions and the following disclaimer.
 |~^~|
 |~^~|-2. Redistributions in binary form must reproduce the above copyright notice,
 |~^~|this list of conditions and the following disclaimer in the documentation
 |~^~|and/or other materials provided with the distribution.
 |~^~|
 |~^~|-3. Neither the name of the copyright holder nor the names of its contributors
 |~^~|may be used to endorse or promote products derived from this software without
 |~^~|specific prior written permission.
 |~^~|
 |~^~|THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 |~^~|AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 |~^~|IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 |~^~|DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 |~^~|FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 |~^~|DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 |~^~|SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 |~^~|CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 |~^~|OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 |~^~|OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.\*/
/**
 *
 */
package edu.mit.ll.nics.android.api.database.tables;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import android.content.ContentValues;
import android.content.Context;
import android.util.Log;
import net.sqlcipher.Cursor;
import net.sqlcipher.database.SQLiteDatabase;

import com.google.gson.Gson;

import edu.mit.ll.nics.android.api.data.ReportSendStatus;
import edu.mit.ll.nics.android.api.data.WeatherReportData;
import edu.mit.ll.nics.android.api.payload.forms.WeatherReportPayload;
import edu.mit.ll.nics.android.utils.Constants;

/**
 * @author Glenn L. Primmer
 *
 * This class contains all the items necessary for creating and accessing a simple report table in the
 * nics database.
 */
public class WeatherReportTable extends DatabaseTable <WeatherReportPayload> {
    /**
     * Defines the columns and its SQLite data type.
     */
    private static final Map<String, String> TABLE_COLUMNS_MAP = new HashMap<String, String> () {
		private static final long serialVersionUID = 4292279082677888054L;
		{
		    put("id",				"integer primary key autoincrement");
		    put("isDraft",			"integer");
		    put("isNew",			"integer");
		    put("seqnum",			"integer");
		    put("seqtime",			"integer");
		    put("incidentId",		"integer");
		    put("user",				"text");
		    
		    put("datasource",		"integer");
		    put("latitude",			"text");
		    put("longitude",		"text");
		    put("elevation",		"text");
		    put("drybulbtemp",		"text");
		    put("wetbulbtemp",		"text");
		    put("relativehumidity",	"text");
		    put("winddirection",	"integer");
		    put("windspeed",		"text");
		    put("aspect",			"integer");
		    put("physicallocation",	"text");
		    put("timetaken",		"text");
		    
		    put ("status",			"text");
		    put ("sendStatus",		"integer");
		    put("json",				"text");
	    }
	};

    /**
     * Constructor.
     *
     * @param tableName Name of the table.
     *
     * @param context Android context reference.
     */
    public WeatherReportTable (final String           tableName,
                              final Context context) {
        super (tableName,
               context);
    }

    @Override
    public void createTable (SQLiteDatabase database) {
        createTable (TABLE_COLUMNS_MAP, database);
    }

    @Override
    public long addData (final WeatherReportPayload data,
                         SQLiteDatabase database) {
        long row = 0L;

        if (database != null) {
            try {
                ContentValues contentValues = new ContentValues ();
                
                contentValues.put("isDraft",		data.isDraft());
                contentValues.put("isNew",			data.isNew());
                contentValues.put("seqnum",			data.getSeqNum());
                contentValues.put("seqtime",		data.getSeqTime());
                contentValues.put("incidentId",		data.getIncidentId());
                
                WeatherReportData messageData = data.getMessageData();
                contentValues.put("user",			messageData.getUser());
                contentValues.put("datasource",		messageData.getDataSource().getId());
                contentValues.put("latitude",		messageData.getLatitude());
                contentValues.put("longitude",		messageData.getLongitude());
                contentValues.put("elevation",		messageData.getElevation());
                contentValues.put("drybulbtemp",	messageData.getDryBulbTemp());
                contentValues.put("wetbulbtemp",	messageData.getWetBulbTemp());
                contentValues.put("relativehumidity",messageData.getRelativeHumidity());
                contentValues.put("winddirection",	messageData.getWindDirection().getId());
                contentValues.put("windspeed",		messageData.getWindSpeed());
                contentValues.put("aspect",			messageData.getAspect().getId());
                contentValues.put("physicallocation",messageData.getPhysicalLocation());
                contentValues.put("timetaken",		messageData.getTimeTaken());
                contentValues.put("status",			messageData.getStatus());
                
                contentValues.put("sendStatus",     data.getSendStatus().getId());
                contentValues.put("json",			data.toJsonString());
              
                row = database.insert (tableName, null, contentValues);
                
            } catch (Exception ex) {
                Log.w (Constants.nics_DEBUG_ANDROID_TAG, "Exception occurred while trying to add data to table: \"" + tableName + "\"", ex);
            }
        } else {
            Log.w (Constants.nics_DEBUG_ANDROID_TAG, "Could not get database to add data to table: \"" + tableName + "\"");
        }

        return row;
    }


    public ArrayList<WeatherReportPayload> getAllDataReadyToSend (long collaborationRoomId, SQLiteDatabase database) {
        String orderBy = "seqtime DESC";
        String sqlSelection = "sendStatus==? AND incidentId==?";
        String[] sqlSelectionArguments = {String.valueOf(ReportSendStatus.WAITING_TO_SEND.getId ()), String.valueOf(collaborationRoomId)};

        return getData(sqlSelection, sqlSelectionArguments, orderBy,NO_LIMIT, database);
    }
    
    public ArrayList<WeatherReportPayload> getAllDataReadyToSend (String orderBy, SQLiteDatabase database) {
        String sqlSelection = "sendStatus==?";
        String[] sqlSelectionArguments = {String.valueOf (ReportSendStatus.WAITING_TO_SEND.getId ())};

        return getData(sqlSelection, sqlSelectionArguments, orderBy,NO_LIMIT, database);
    }
    

    public ArrayList<WeatherReportPayload> getAllDataHasSent (long collaborationRoomId, SQLiteDatabase database) {
        String orderBy = "seqtime DESC";
        String sqlSelection = "sendStatus==? AND incidentId==?";
        String[] sqlSelectionArguments = {String.valueOf(ReportSendStatus.SENT.getId ()), String.valueOf(collaborationRoomId)};

        return getData(sqlSelection, sqlSelectionArguments, orderBy,NO_LIMIT, database);
    }
    
    public ArrayList<WeatherReportPayload> getAllDataHasSent (String orderBy, SQLiteDatabase database) {
        String sqlSelection = "sendStatus==?";
        String[] sqlSelectionArguments = {String.valueOf (ReportSendStatus.SENT.getId ())};

        return getData(sqlSelection, sqlSelectionArguments, orderBy,NO_LIMIT, database);
    }

    @Override
    protected ArrayList<WeatherReportPayload> getData (String   sqlSelection, String[] sqlSelectionArguments, String   orderBy, String limit, SQLiteDatabase database) {
    	ArrayList<WeatherReportPayload> dataList = new ArrayList<WeatherReportPayload> ();

        if (database != null) {
            try {
                Cursor cursor;
                if (sqlSelection == null) {
                    cursor = database.query(tableName,                                                                   // Table
                                            TABLE_COLUMNS_MAP.keySet ().toArray (new String[TABLE_COLUMNS_MAP.size ()]), // Columns
                                            null,                                                                        // Selection
                                            null,                                                                        // Selection arguments
                                            null,                                                                        // Group by
                                            null,                                                                        // Having
                                            orderBy,
                                            limit);                                                                       // Order by
                } else {
                    cursor = database.query(tableName,                                                                   // Table
                                            TABLE_COLUMNS_MAP.keySet ().toArray (new String[TABLE_COLUMNS_MAP.size ()]), // Columns
                                            sqlSelection,                                                                // Selection
                                            sqlSelectionArguments,                                                       // Selection arguments
                                            null,                                                                        // Group by
                                            null,                                                                        // Having
                                            orderBy,
                                            limit);                                                                    // Order by
                }

                if (cursor != null) {
                    cursor.moveToFirst();

                    while (!cursor.isAfterLast()) {
                        // Unfortunately, the use of having things simplified in the table constructor leaves us having
                        // to make 2 calls for every data element retrieved.  However, the code is easier to follow.
                        WeatherReportPayload dataItem = new Gson().fromJson(cursor.getString(cursor.getColumnIndex("json")), WeatherReportPayload.class);
                        dataItem.setId(cursor.getLong(cursor.getColumnIndex("id")));
                        dataItem.setSendStatus(ReportSendStatus.lookUp(cursor.getInt(cursor.getColumnIndex("sendStatus"))));
                        dataItem.setDraft(cursor.getInt(cursor.getColumnIndex("isDraft")) > 0 ? true : false);
                        dataItem.setNew(cursor.getInt(cursor.getColumnIndex("isNew")) > 0 ? true : false);
                        dataItem.parse();
                        
                        dataList.add(dataItem);

                        cursor.moveToNext();
                    }

                    cursor.close();
                }
            } catch (Exception ex) {
                Log.w (Constants.nics_DEBUG_ANDROID_TAG,
                       "Exception occurred while trying to get data from table: \"" + tableName + "\"",
                       ex);
            }
        } else {
            Log.w (Constants.nics_DEBUG_ANDROID_TAG,
                   "Could not get database to get all data from table: \"" + tableName + "\"");
        }

        return dataList;
    }

    /**
     * Gets the last data that was received and stored into the database.
     *
     * @param database The database.
     *
     * @return The timestamp of the last message received or (-1L) if no messages were received for that chat room.
     */
    public long getLastDataTimestamp (SQLiteDatabase database) {
        long lastMessageTimestamp = -1L;

        if (database != null) {
            try {
                // Descending by timestamp so that the newest item is the first item returned.
                String   orderBy               = "seqtime DESC";

                Cursor cursor = database.query(tableName,                                                                   // Table
                                               TABLE_COLUMNS_MAP.keySet ().toArray (new String[TABLE_COLUMNS_MAP.size ()]), // Columns
                                               null,                                                                        // Selection
                                               null,                                                                        // Selection arguments
                                               null,                                                                        // Group by
                                               null,                                                                        // Having
                                               orderBy);                                                                    // Order by

                if (cursor != null) {
                    cursor.moveToFirst();

                    // First record is our newest item (largest timestamp).
                    if (!cursor.isAfterLast ()) {
                    	int colIdx = cursor.getColumnIndex ("seqtime");
                        if(colIdx > -1) {
                        	lastMessageTimestamp = cursor.getLong(colIdx);
                        }
                    }

                    cursor.close();
                }
            } catch (Exception ex) {
                Log.w (Constants.nics_DEBUG_ANDROID_TAG,
                       "Exception occurred while trying to get data from table: \"" + tableName + "\"",
                       ex);
            }
        } else {
            Log.w (Constants.nics_DEBUG_ANDROID_TAG,
                   "Could not get database to get all data from table: \"" + tableName + "\"");
        }

        return lastMessageTimestamp;
    }
    
	public long getLastDataForIncidentTimestamp(long incidentId, SQLiteDatabase database) {
		long lastMessageTimestamp = -1L;

		if (database != null) {
			try {
				// Descending by time-stamp so that the newest item is the first item returned.
				String orderBy = "seqtime DESC";
				String sqlSelection = "incidentId==?";
				String[] sqlSelectionArguments = { String.valueOf(incidentId) };

				Cursor cursor = database.query(tableName, TABLE_COLUMNS_MAP.keySet().toArray(new String[TABLE_COLUMNS_MAP.size()]), sqlSelection, sqlSelectionArguments, null, null, orderBy);

				if (cursor != null) {
					cursor.moveToFirst();

					// First record is our newest item (largest time-stamp).
					if (!cursor.isAfterLast()) {
						lastMessageTimestamp = cursor.getLong(cursor.getColumnIndex("seqtime"));
					}

					cursor.close();
				}
			} catch (Exception ex) {
				Log.w(Constants.nics_DEBUG_ANDROID_TAG, "Exception occurred while trying to get data from table: \"" + tableName + "\"", ex);
			}
		} else {
			Log.w(Constants.nics_DEBUG_ANDROID_TAG, "Could not get database to get all data from table: \"" + tableName + "\"");
		}

		return lastMessageTimestamp;
	}

	public WeatherReportPayload getLastDataForIncidentId(long incidentId, SQLiteDatabase database) {
		WeatherReportPayload lastPayload = null;

		if (database != null) {
			try {
				// Descending by time-stamp so that the newest item is the first item returned.
				String orderBy = "seqtime DESC";
				String sqlSelection = "incidentId==?";
				String[] sqlSelectionArguments = { String.valueOf(incidentId) };

				Cursor cursor = database.query(tableName, TABLE_COLUMNS_MAP.keySet().toArray(new String[TABLE_COLUMNS_MAP.size()]), sqlSelection, sqlSelectionArguments, null, null, orderBy);

				if (cursor != null) {
					cursor.moveToFirst();

					// First record is our newest item (largest time-stamp).
					if (!cursor.isAfterLast()) {
						lastPayload = new Gson().fromJson(cursor.getString(cursor.getColumnIndex("json")), WeatherReportPayload.class);
					}

					cursor.close();
				}
			} catch (Exception ex) {
				Log.w(Constants.nics_DEBUG_ANDROID_TAG, "Exception occurred while trying to get data from table: \"" + tableName + "\"", ex);
			}
		} else {
			Log.w(Constants.nics_DEBUG_ANDROID_TAG, "Could not get database to get all data from table: \"" + tableName + "\"");
		}
		return lastPayload;
	}
	
	public ArrayList<WeatherReportPayload> getDataForIncident(long collaborationRoomId, SQLiteDatabase database) {
        String   orderBy               = "seqtime DESC";
        String   sqlSelection          = "incidentId==?";
        String[] sqlSelectionArguments = {String.valueOf (collaborationRoomId)};

        return getData (sqlSelection, sqlSelectionArguments, orderBy,NO_LIMIT, database);
	}

	@Override
	public long addData(ArrayList<WeatherReportPayload> data, SQLiteDatabase database) {
		return 0;
	}

	public ArrayList<WeatherReportPayload> getDataByReportId(int reportId, SQLiteDatabase database) {
        String   orderBy               = "seqtime DESC";
        String   sqlSelection          = "id==?";
        String[] sqlSelectionArguments = {String.valueOf (reportId)};

        return getData (sqlSelection, sqlSelectionArguments, orderBy,NO_LIMIT, database);
	}
}
