/*
 * Copyright (C) 2015 Andrey Rychkov <wholegroup@gmail.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.wholegroup.rally;

import android.app.ListActivity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.BaseAdapter;
import android.widget.TextView;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONStringer;
import java.util.Arrays;

/** */
public class ScoreActivity extends ListActivity
{
	/** Количество записей в таблице рекордов. */
	private final static int RECORDCOUNT = 10;
	
	/** Массив записей таблицы рекордов. */
	private ScoreRecord[] m_arrRecords;
	
	/** Набранные очки. */
	private int m_iScore;
	
	/** Тип игры. */
	private int m_iType;
	
	/** Игра А. */
	private String m_strGameA; 
	
	/** Игра Б. */
	private String m_strGameB;

	/** Последнее вводимое имя пользователя. */
	private String m_sName;

	/** Для вывода статистики на экран. */
	public StringBuffer m_sbNumber;

	/** */
	@Override 
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		
		// буффер для вывода числовой статистики
		m_sbNumber = new StringBuffer();

 		// инициализация
		Resources m_res = getResources();
   	
		String[] arrDefaultNames  = m_res.getStringArray(R.array.names);
   	int[]    arrDefaultScores = m_res.getIntArray(R.array.scores);

   	m_arrRecords = new ScoreRecord[RECORDCOUNT];
 		
 		for (int i = 0; i < RECORDCOUNT; i++)
 		{
 			if ((i < arrDefaultNames.length) && (i < arrDefaultScores.length))
 			{
 	 	 		m_arrRecords[i] = new ScoreRecord(arrDefaultNames[i], arrDefaultScores[i], Rally.GAME_A);
 			}
 			else
 			{
 	 	 		m_arrRecords[i] = new ScoreRecord(getString(R.string.score_default_name), 0, Rally.GAME_A);
 			}
 		}

   	m_strGameA = getString(R.string.rally_text_short_game_a);   	
   	m_strGameB = getString(R.string.rally_text_short_game_b);
 		
 		// получение переданных параметров
 		Intent intent = getIntent();
 		
 		m_iScore = intent.getIntExtra(getString(R.string.score_parameter_score), 0);
 		m_iType  = intent.getIntExtra(getString(R.string.score_parameter_type), Rally.GAME_A);
 		m_sName  = getString(R.string.score_default_name);
 		
 		// загрузка таблицы рекордов
      LoadScore();   	
       
 		// сортировка массива
 		Arrays.sort(m_arrRecords);

   	// установка видимого слоя
   	requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
		setContentView(R.layout.score);
 		
   	// установка заголовка окна
   	getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.custom_title);

      ((TextView)findViewById(R.id.custom_title_left_text)).setText(R.string.score_activity_title);
      ((TextView)findViewById(R.id.custom_title_right_text)).setText(R.string.application_upper);

      // установка адаптера для формирования списка
 		setListAdapter(new ScoreAdapter());

 		// вывод диалога ввода имени для нового рекорда
 		if ((0 < m_iScore) && (m_iScore > m_arrRecords[RECORDCOUNT - 1].m_iScore))
 		{
 	 		showDialog(0);
 		}
	}
	
   /**
    * Загрузка таблицы рекордов.
    */
   private void LoadScore()
   {
   	SharedPreferences settings = getSharedPreferences(getString(R.string.preferences_id), 0);
   	m_sName = settings.getString(getString(R.string.preferences_score_oldname),
			getString(R.string.score_default_name));
   	String sArray = settings.getString(getString(R.string.preferences_score_array), "");
   	
   	if (0 < sArray.length())
   	{
      	try
      	{
         	JSONArray jsonArray = new JSONArray(sArray);
         	
         	for (int i = 0; i < jsonArray.length(); i++)
         	{
         		if (i < RECORDCOUNT)
         		{
            		m_arrRecords[i].fromString(jsonArray.getString(i));
         		}
         	}
      	}
   		catch (JSONException e)
   		{
   			e.printStackTrace();
   		}
   	}
   }
   
   /**
    * Сохранение таблицы рекордов.
    */
   private void SaveScore()
   {
   	String sArray = "";

 		try
 		{
 			sArray = new JSONArray(Arrays.toString(m_arrRecords)).toString();
 		}
 		catch (JSONException e)
 		{
 			e.printStackTrace();
 		}

 		SharedPreferences settings = getSharedPreferences(getString(R.string.preferences_id), 0);
   	SharedPreferences.Editor editor = settings.edit();

   	editor.putString(getString(R.string.preferences_score_oldname), m_sName);
   	editor.putString(getString(R.string.preferences_score_array), sArray);

   	editor.commit();
   }
	

   /**
    * Создание диалога для ввода имени
    */
   @Override
   protected Dialog onCreateDialog(int id)
   {
   	final View textEntryView = View.inflate(this, R.layout.score_dialog, null);

   	final TextView textName = (TextView)textEntryView.findViewById(R.id.score_dlg_username_edit);

   	textName.setText(m_sName);
   	
   	return new AlertDialog.Builder(ScoreActivity.this)
   		.setView(textEntryView)
   		.setTitle(getString(R.string.score_dialog_name))
   		.setPositiveButton("OK", new DialogInterface.OnClickListener()
   			{
   				public void onClick(DialogInterface dialog, int whichButton)
   				{
   					m_sName = textName.getText().toString();
   					if (0 == m_sName.trim().length())
   					{
   						m_sName = getString(R.string.score_default_name);
   					}
   					
   					// замена последнего элемента массива на новые данные
   					m_arrRecords[ScoreActivity.RECORDCOUNT - 1] = new ScoreRecord(
							m_sName, m_iScore, m_iType);

   					// сортировка массива
   					Arrays.sort(m_arrRecords);

   					// обновление списка рекордов
   					((ScoreAdapter)ScoreActivity.this.getListAdapter()).notifyDataSetChanged();
   					
   					// сохранение таблицы рекордов
   					SaveScore();
   					
   					// обнуление параметра, чтобы при повороте экрана
   					// не происходило дублирование ввода имени
   			 		getIntent().putExtra(getString(R.string.score_parameter_score), 0);
   				}
   			}
   		)
   		.create();
   }
   
   /**
    * Класс одной записи в таблице рекордов.
    */
   public class ScoreRecord implements Comparable<ScoreRecord>
   {
   	/** Имя. */
   	private String m_sName;
   	
   	/** Количество очков. */
   	private int m_iScore = 0;
   	
   	/** Тип игры.*/
   	private int m_iType = Rally.GAME_A;
   	
   	/**
   	 * Конструктор.
   	 */
   	public ScoreRecord(String sName, int iScore, int iType)
   	{
   		m_sName  = sName;
   		m_iScore = iScore;
   		m_iType  = iType;
   	}

		/** */
   	@Override
   	public String toString()
   	{
   		String sReturn="";
   		
   		try
			{
   			sReturn = new JSONStringer()
   			.object()
   				.key("m_sName").value(m_sName)
   				.key("m_iScore").value(m_iScore)
   				.key("m_iType").value(m_iType)
   			.endObject()
   			.toString();
   		}
			catch (JSONException e)
			{
				e.printStackTrace();
			}
   		 
   		return sReturn;
   	}

		/** */
   	public void fromString(String sObject)
   	{
   		try
			{
   			JSONObject jsonObject = new JSONObject(sObject);
   			
   			m_sName  = jsonObject.getString("m_sName");
   			m_iScore = jsonObject.getInt("m_iScore");
   			m_iType  = jsonObject.getInt("m_iType");
   		}
			catch (JSONException e)
			{
				e.printStackTrace();
			}
   	}

		/** */
   	@SuppressWarnings("NullableProblems")
		public int compareTo(ScoreRecord srCompare)
   	{
   		if (m_iScore < srCompare.m_iScore)
   		{
   			return 1;
   		}
   		
   		if ((m_iScore == srCompare.m_iScore) && (m_iType < srCompare.m_iType))
   		{
   			return 1;
   		}
   		
   		return -1;
   	}
   }
   
   /**
    * Адаптер для вывода списка.
    */
   private class ScoreAdapter extends BaseAdapter
   {
		/** */
   	public int getCount()
   	{
   		return m_arrRecords.length;
   	}

		/** */
   	public Object getItem(int position)
   	{
   		return position;
   	}

		/** */
   	public long getItemId(int position)
   	{
   		return position;
   	}

		/** */
   	public View getView(int position, View convertView, ViewGroup parent)
   	{
			View viewContent;
   		
   		if (null == convertView)
   		{
   			viewContent = View.inflate(ScoreActivity.this, R.layout.score_item, null);
   		}
   		else
   		{
   			viewContent = convertView;
   		}

   		TextView tv;
   		
   		// вывод номера
   		tv = (TextView)viewContent.findViewById(R.id.score_item_number);

   		tv.setText(com.wholegroup.common.insertInt(m_sbNumber, position + 1));

   		// вывод имени
   		tv = (TextView)viewContent.findViewById(R.id.score_item_name);

   		tv.setText(m_arrRecords[position].m_sName);

   		// вывод очков
   		tv = (TextView)viewContent.findViewById(R.id.score_item_score);

   		tv.setText(com.wholegroup.common.insertInt(m_sbNumber, m_arrRecords[position].m_iScore));

   		// вывод типа игры
   		tv = (TextView)viewContent.findViewById(R.id.score_item_type);

   		if (Rally.GAME_B == m_arrRecords[position].m_iType)
   		{
      		tv.setText(m_strGameB);
   		}
   		else
   		{
   			tv.setText(m_strGameA);
   		}
   		
   		return viewContent;
   	}
   }
}
