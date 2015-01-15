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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/** */
public class Rally
{
	/** Генератор случайных чисел. */
	private static final Random RND = new Random(); 

	/** Константа для задания игры А. */
	public final static int GAME_A = 0;

	/** Константа для задания игры Б. */
	public final static int GAME_B = 1;
	
	/** Константа для ДЕМО игры. */
	public final static int GAME_DEMO = 2;

	/** Ширина игрового поля. */
	public final static int FIELDWIDTH = 3;
	
	/** Высота игрового поля. */
	public final static int FIELDHEIGHT = 6;

	/** Количество позиций игрока. */
	public final static int PLAYERPOSMAX = 3;

	/** Стартовое количество жизней. */
	public final static int LIFEMAX = 3;
	
	/** Период появления бордюра. */
	public final static int PERIOD_BORDER = 3;
	
	/** Плотность препятствий. */
	public final static int DENSITY_GAME[] = {
		(int)(0.33334f * (Rally.FIELDHEIGHT * Rally.FIELDWIDTH)),
		(int)(0.6f * (Rally.FIELDHEIGHT * Rally.FIELDWIDTH)),
	};
	
	/** Максимальная скорость (в мс). */
	public static final int SPEEDMAX = 300;
	
	/** Минимальная скорость (в мс). */
	public static final int SPEEDMIN = 600;
	
	/** Шаг увеличения скорости (в мс). */
	public static final int SPEEDSTEP = 30;
	
	/** Количество очков для увеличения скорости. */
	public static final int SCORESPEEDUP = 15; 
	
	/** Игровое поле. */
	public int[][] m_arrField;
	
	/** Бордюры. */
	public int[] m_arrBorders;
	
	/** Отсчет периода появления бордюра. */
	public int m_iPeriodBorder;

	/** Тип игры. */
	public int m_iTypeGame;
	
	/** Позиция игрока. */
	public int m_iPlayerPos;
	
	/** Шаг сдвига позиции в демо режиме. */
	private int m_iPlayerPosStepDemo;
	
	/** Количество жизней. */
	public int m_iLifeCount;

	/** Количество очков. */
	public int m_iScore;
	
	/** Плотность препятствий. */
	public int m_iDensity;
	
	/** Массив для функции генерирования препятствий. */
	private ArrayList<Integer> m_arrBlocked;
	
	/** Скорость в мс. */
	public int m_iSpeedMS;
	
	/**
	 * Конструктор.
	 */
	public Rally()
	{
		m_arrField   = new int[FIELDHEIGHT][FIELDWIDTH];
		m_iTypeGame  = GAME_A;
		m_arrBorders = new int[FIELDHEIGHT];
		m_arrBlocked = new ArrayList<Integer>();
	}
	
	
	/**
	 * Инициализация переменных.
	 */
	private void init()
	{
		clearField();

		m_iPeriodBorder      = 0;
		m_iPlayerPos         = PLAYERPOSMAX / 2;
		m_iPlayerPosStepDemo = -1;
		m_iLifeCount         = LIFEMAX;
		m_iScore             = 0;
		m_iSpeedMS           = SPEEDMIN;
	}
	
	/**
	 * Старт игры.
	 */
	public void startGame(int iTypeGame)
	{
		m_iTypeGame = iTypeGame;

		init();
	}
	
	/**
	 * Движение влево.
	 */
	public boolean moveLeft()
	{
		m_iPlayerPos--;
		
		if (m_iPlayerPos < 0)
		{
			m_iPlayerPos = 0;

			return false;
		}
		
		return true;
	}
	
	/**
	 * Движение вправо.
	 */
	public boolean moveRight()
	{
		m_iPlayerPos++;
		
		if (m_iPlayerPos >= PLAYERPOSMAX)
		{
			m_iPlayerPos = PLAYERPOSMAX - 1;
			
			return false;
		}
		
		return true;
	}
	
	/**
	 * Движение вперед.
	 */
	public boolean moveForward()
	{
		// в демо режиме автоматически объезжаем препятствия
		if ((GAME_DEMO == m_iTypeGame) && (1 == m_arrField[FIELDHEIGHT - 1][m_iPlayerPos]))
		{
			do
			{
				m_iPlayerPos += m_iPlayerPosStepDemo;
				
				if (0 >= m_iPlayerPos)
				{
					m_iPlayerPosStepDemo = 1;
				}
				
				if (FIELDWIDTH <= (m_iPlayerPos + 1))
				{
					m_iPlayerPosStepDemo = -1;
				}
				
			} while ((m_iPlayerPos < 0) || (m_iPlayerPos >= FIELDWIDTH) ||
				(1 == m_arrField[FIELDHEIGHT - 1][m_iPlayerPos]));
		}
		
		// проверка возможности выполнить движение
		if (1 == m_arrField[FIELDHEIGHT - 1][m_iPlayerPos])
		{
			return false;
		}

		// начисление очков
		if (GAME_DEMO != m_iTypeGame)
		{
			for (int x = 0; x < FIELDWIDTH; x++)
			{
				if (0 != m_arrField[FIELDHEIGHT - 1][x])
				{
					m_iScore++;
					
					if ((m_iSpeedMS > SPEEDMAX) && (0 == (m_iScore % SCORESPEEDUP))) 
					{
						m_iSpeedMS -= SPEEDSTEP;
					}
					
					break;
				}
			}
		}

		// сдвигаем трассу
		for (int y = (FIELDHEIGHT - 1); y > 0; y--)
		{
			System.arraycopy(m_arrField[y - 1], 0, m_arrField[y], 0, FIELDWIDTH);
		}
		
		for (int x = 0; x < FIELDWIDTH; x++)
		{
			if ((GAME_DEMO != m_iTypeGame) && (0 < m_arrField[0][x]))
			{
				m_iDensity--; 
			}

			m_arrField[0][x] = 0;
		}
		
		// сдвигаем бордюры
		System.arraycopy(m_arrBorders, 0, m_arrBorders, 1, FIELDHEIGHT - 1);
		
		if (0 >= (PERIOD_BORDER - m_iPeriodBorder))
		{
			m_iPeriodBorder = 0;
		}
		
		if (0 == m_iPeriodBorder)
		{
			m_arrBorders[0] = 1;
		}
		else
		{
			m_arrBorders[0] = 0;
		}
		
		m_iPeriodBorder++;
		
		// генерируем новые препятствия
		if (GAME_DEMO == m_iTypeGame)
		{
			m_arrField[0][RND.nextInt(FIELDWIDTH)] = 1;
		}
		else
		{
			if (m_iDensity < DENSITY_GAME[m_iTypeGame])
			{
				int iCount = RND.nextInt(FIELDWIDTH);

				if ((m_iDensity + iCount) > DENSITY_GAME[m_iTypeGame])
				{
					iCount = DENSITY_GAME[m_iTypeGame] - m_iDensity;
				}

				m_iDensity += iCount;

				m_arrBlocked.clear();
				
				for (int i = 0 ; i < Rally.FIELDWIDTH; i++)
				{
					m_arrBlocked.add(i);
				}
				
				while (iCount > 0)
				{
					int iNumber = RND.nextInt(m_arrBlocked.size());

					m_arrField[0][m_arrBlocked.get(iNumber)] = 1;
					m_arrBlocked.remove(iNumber);
					
					iCount--;
				}
			}
		}
		
		return true;
	}

	/**
	 * Сохраняет объект в виде JSON строки.
	 */
	public String toJSON()
	{
		String strJSON = "";
		
		try
		{
			strJSON = new JSONObject()
				.put("m_arrField", new JSONArray(Arrays.deepToString(m_arrField)))
				.put("m_iTypeGame", m_iTypeGame)
				.put("m_iPlayerPos", m_iPlayerPos)
				.put("m_iDensity", m_iDensity)
				.put("m_iLifeCount", m_iLifeCount)
				.put("m_iScore", m_iScore)
				.put("m_iSpeedMS", m_iSpeedMS)
				.toString();
		}
		catch (JSONException e)
		{
			e.printStackTrace();
		}
		
		return strJSON;
	}

	/**
	 * Восстанавливает объект из JSON строки.
	 */
	public void fromJSON(String strJSON)
	{
   	try
   	{
			JSONObject jsonObject = new JSONObject(strJSON);
   		JSONArray  jsonArray;
   		JSONArray  jsonArray2;
   		
   		jsonArray = jsonObject.getJSONArray("m_arrField");

      	for (int y = 0; y < jsonArray.length(); y++)
      	{
      		if (FIELDHEIGHT <= y )
      		{
      			break;
      		}
      		
      		jsonArray2 = jsonArray.getJSONArray(y);

      		for (int x = 0; x < jsonArray2.length(); x++)
      		{
      			if (FIELDWIDTH <= x)
      			{
      				break;
      			}
      			
      			m_arrField[y][x] = jsonArray2.getInt(x);
      		}
      	}

      	m_iScore     = jsonObject.getInt("m_iScore");
      	m_iTypeGame  = jsonObject.getInt("m_iTypeGame");
      	m_iPlayerPos = jsonObject.getInt("m_iPlayerPos");
      	m_iLifeCount = jsonObject.getInt("m_iLifeCount");
      	m_iDensity   = jsonObject.getInt("m_iDensity");
      	m_iSpeedMS   = jsonObject.getInt("m_iSpeedMS"); 
   	}
		catch (JSONException e)
		{
			e.printStackTrace();
		}
	}

	/**
	 * Уменьшение жизни на 1.
	 * - инициализация поля
	 */
	public boolean decLife()
	{
		if (1 >= m_iLifeCount)
		{
			return false;
		}
		
		m_iLifeCount--;
		
		clearField();

		m_iPlayerPos = PLAYERPOSMAX / 2;
		
		return true;
	}
	
	
	/**
	 * Очистка поля.
	 */
	private void clearField()
	{
		for (int y = 0; y < FIELDHEIGHT; y++)
		{
			for (int x = 0; x < FIELDWIDTH; x++)
			{
				m_arrField[y][x] = 0;
			}
			
			m_arrBorders[y] = 0;
		}
		
		m_iDensity = 0;
	}
}
