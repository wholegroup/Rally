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

package com.wholegroup;

public final class common
{
	/** Набор цифр для использования в функции insertInt */
	final static char[] m_cNumber = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9'};

	/**
	 * Функция вставляет положительный int в строкой буфер.
	 * 
	 * @param sbIn строковой буфер
	 * @param iAdd число
	 *
	 * @return строковой буфер 
	 */

	public static StringBuffer insertInt(StringBuffer sbIn, int iAdd)
	{
		return insertInt(sbIn, iAdd, true);
	}
	
	public static StringBuffer insertInt(StringBuffer sbIn, int iAdd, boolean bSpace)
	{
		// очищаем строку
		sbIn.delete(0, sbIn.length());

		// нулевое число сразу возвращаем
		if (0 == iAdd)
		{
			return sbIn.append('0');
		}
		
		// вставляем цифры по очереди
		int iNumber;
		int iCount = 0;

		while (iAdd > 0)
		{
			// разделитель после 3 цифр
			if (bSpace && (0 == (iCount % 3)))
			{
				sbIn.insert(0, ' ');
			}
			
			iCount++;
			
			iNumber = iAdd % 10;
			iAdd -= iNumber;
			iAdd /= 10;
			
			sbIn.insert(0, m_cNumber[iNumber]);
		}
		
		return sbIn;
	}
}
