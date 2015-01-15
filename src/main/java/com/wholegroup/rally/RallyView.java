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

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.text.TextPaint;
import android.text.format.DateFormat;
import android.util.AttributeSet;
import android.view.View;
import java.util.Date;

public class RallyView extends View
{
	/** Максимальное количество цифр для вывода очков. */
	public final static int OUTPUT_NUMBERS_MAX = 5;
	
	/** Отношение горизонтального расстояния между полосами к длине полосы. */
	private final static float LANE_RELATION = 0.4f;
	
	/** Угол наклона левой стороны трассы. */
	private final static double ANGLE_LEFT = Math.PI / 6.0f;
	
	/** Угол наклона правой стороны трассы. */
	private final static double ANGLE_RIGHT = Math.PI / 2.5f;
	
	/** Относительные расстояния между элементами трассы (от 0 до 1, x1-x2). */
	private final static float TRACK_DELIMITER[] = {
		0.00f, 0.03f,     // левая линия
		0.07f, 0.14f,     // бордюр
		0.18f, 0.82f,     // дорога
		0.86f, 0.93f,     // бордюр
		0.97f, 1.00f};    // правая линия
	
	/** Индекс расстояния левой полосы. */
	private final static int TRACK_DELIMITER_INDEX_LEFT_LINE = 0;

	/** Индекс расстояния левого бордюра. */
	private final static int TRACK_DELIMITER_INDEX_LEFT_BORDER = 2;

	/** Индекс расстояния дороги. */
	private final static int TRACK_DELIMITER_INDEX_ROAD = 4;

	/** Индекс расстояния правого бордюра. */
	private final static int TRACK_DELIMITER_INDEX_RIGHT_BORDER = 6;

	/** Индекс расстояния правой полосы. */
	private final static int TRACK_DELIMITER_INDEX_RIGHT_LINE = 8;
	
	/** Ширина изображения с флагом. */
	private final static int FLAGWIDTH = 128;
	
	/** Высота изображения с флагом. */
	private final static int FLAGHEIGHT = 107;

	/** Ширина цифры. */
	private final static int NUMBERWIDTH = 35;
	
	/** Высота цифры. */
	private final static int NUMBERHEIGHT = 64; 
	
	/** Ссылка на родительский класс PlayActivity. */
	private RallyActivity m_activity; 
	
	/** Ссылка на ресурсы. */
	private Resources m_res;

	/** Объекты игры Tetroid. */
	public Rally m_cRally;
	
	/** Ширина экрана. */
	private int m_iWidth;

	/** Высота экрана. */
	private int m_iHeight;
	
	/** Ширина машины. */
	private int m_iCarWidth;
	
	/** Высота машины. */
	private int m_iCarHeight;
	
	/** Битмап машины. */
	private Bitmap m_bmpCar[] = new Bitmap[2];
	
	/** Позиции машины. */
	private Point m_ptCar[];

	/** Ширина цифры. */
	private int m_iNumberWidth;

	/** Высота цифры. */
	private int m_iNumberHeight;
	
	/** Горизонтальный отступ. */
	private int m_iNumberDX;

	/** Вертикальный отступ. */
	private int m_iNumberDY;
	
	/** Расстояние между цифрами. */
	private int m_iNumberStep;
	
	/** Битмапы цифр и двоеточие. */
	private Bitmap m_bmpNumbers[] = new Bitmap[11];
	
	/** Ширина флага. */
	private int m_iFlagWidth;
	
	/** Высота флага. */
	private int m_iFlagHeight;

	/** Дата для вывода времени. */
	public Date m_dateCurrent;
	
	/** Размер квадрата для вывода типа игры. */
	private int m_iRectGameType;
	
	/** Битмапы для вывода типа игры: А или Б. */
	private Bitmap m_bmpGameType[] = new Bitmap[2];
	
	/** Формат вывода времени (true = 24, false = 12). */
	private boolean m_bIs24Hour;
	
	/** Битмапы для вывода AM/PM. */
	private Bitmap m_bmp12Hour[] = new Bitmap[2];
	
	/** Массив с координатами для вывода трассы. */
	private Path m_arrTrack[][];

	/** Массив с координатам для отрисовки бордюров. */
	private Path m_arrTrackBorder[][];
	
	/** Кисть для отрисовки трассы. */
	private Paint m_paintField;

	/** Координаты краев трассы. */
	private Path m_arrTrackLine[];

	/** Кисть для отрисовки боковых линий. */
	private Paint m_paintLine;
	
	/** Координаты для отрисовки травы. */
	private Path m_pathGrass;

	/** Конструктор */
	@SuppressWarnings("UnusedDeclaration")
	public RallyView(Context context)
	{
		super(context);

		initView();
	}

	/** Конструктор */
	@SuppressWarnings("UnusedDeclaration")
	public RallyView(Context context, AttributeSet attrs)
	{
		super(context, attrs);

		initView();
	}

	/** Конструктор */
	@SuppressWarnings("UnusedDeclaration")
	public RallyView(Context context, AttributeSet attrs, int defStyle)
	{
		super(context, attrs, defStyle);

		initView();
	}

	/**
	 * Инициализация класса.
	 */
	protected void initView()
	{
		// Ссылка на родительский класс PlayActivity
		m_activity = (RallyActivity)getContext();
		
		// ссылка на ресурсы
		m_res = m_activity.getResources();
		
		// размеры экрана
		m_iWidth = 0;
		m_iHeight = 0;
		
		// координаты позиций машины
		m_ptCar = new Point[Rally.FIELDWIDTH];

		for (int x = 0; x < Rally.FIELDWIDTH; x++)
		{
			m_ptCar[x] = new Point();
		}
		
		// текущая дата
		m_dateCurrent = new Date();

		// получение формата вывода часов
		m_bIs24Hour = DateFormat.is24HourFormat(getContext());
		
		// массивы для отрисовки трассы
		m_arrTrack       = new Path[Rally.FIELDHEIGHT][Rally.FIELDWIDTH];
		m_arrTrackBorder = new Path[2][Rally.FIELDHEIGHT];

		for (int y = 0 ; y < Rally.FIELDHEIGHT; y++)
		{
			for (int x = 0; x < Rally.FIELDWIDTH; x++)
			{
				m_arrTrack[y][x] = new Path();
			}

			// левые бордюры
			m_arrTrackBorder[0][y] = new Path();
			
			// правые бордюры
			m_arrTrackBorder[1][y] = new Path();
		}
		
		// кисть для отрисовки трассы
		m_paintField = new Paint();

		m_paintField.setAntiAlias(true);
		m_paintField.setColor(m_res.getColor(R.color.rally_color_view_paint));
		
		/* Кисть для отрисовки невидимых элементов трассы */
		Paint m_paintFieldInvisible = new Paint();

		m_paintFieldInvisible.setAntiAlias(true);
		m_paintFieldInvisible.setColor(m_res.getColor(R.color.rally_color_view_paint_invisible));
		
		// края трассы
		m_arrTrackLine = new Path[2];

		m_arrTrackLine[0] = new Path();
		m_arrTrackLine[1] = new Path();

		// кисть для отрисовки боковых линий
		m_paintLine = new Paint();

		m_paintLine.setAntiAlias(true);
		m_paintLine.setColor(m_res.getColor(R.color.rally_color_view_line));

		// кисть для отрисовки травы
		Paint m_paintGrass = new Paint();

		m_paintGrass.setAntiAlias(true);
		m_paintGrass.setColor(m_res.getColor(R.color.rally_color_view_grass));
		
		// координаты травы
		m_pathGrass = new Path();
	}

	/** 
	 * Обработка изменения размеров.
	 */
	protected void setSize(int w, int h)
	{
		if ((m_iWidth == w) && (m_iHeight == h))
		{
			return;
		}

		// запоминаем размеры игрового поля
		m_iWidth  = w;
		m_iHeight = h;
		
		// размеры битмапа для вывода типа игры
		m_iRectGameType = (int)(m_iHeight * 0.13f);

		if (0 != m_iRectGameType % 2)
		{
			m_iRectGameType++;
		}

		// размеры машины
		m_iCarWidth = 62;
		m_iCarHeight = 45;

		// размеры трассы
		int m_iTrackWidth  = (int)(m_iWidth * 0.98f);
		int m_iTrackHeight = m_iHeight - m_iCarHeight;

		// вертикальный шаг одной полосы
		float fTrackOneStep = m_iTrackHeight / (Rally.FIELDHEIGHT * 5 - 4);
		
		// высота одной полосы (3/4 от шага) в пикселах
		float fTrackOneHeight = fTrackOneStep * 3.0f / 4.0f;

		// относительная длина полосы, расчет по формуле 
		// K * X1 + (K - 1) * X2 = (TRACK_DELIMITER[5] - TRACK_DELIMITER[4])
		// X1 - длина полосы
		// X2 - длина расстояния
		// K - количество полос
		// отношение X2/X1 = LANERELATION
		float fLane     = (TRACK_DELIMITER[TRACK_DELIMITER_INDEX_ROAD + 1] -
			TRACK_DELIMITER[TRACK_DELIMITER_INDEX_ROAD]) /
			(Rally.FIELDWIDTH + LANE_RELATION * (Rally.FIELDWIDTH - 1));
		float fLaneStep = fLane * LANE_RELATION;
		
		// относительные горизонтальные координаты полос (от 0 до 1, в процентах)
		float arrLane[] = new float[Rally.FIELDWIDTH * 2];

		for (int i = 0; i < (Rally.FIELDWIDTH * 2); i += 2)
		{
			arrLane[i]     = (i / 2) * (fLane + fLaneStep) + TRACK_DELIMITER[TRACK_DELIMITER_INDEX_ROAD]; 
			arrLane[i + 1] = arrLane[i] + fLane;  
		}
		
		// тангенсы углов
		float fAngleFirst = (float)Math.tan(ANGLE_LEFT);
		float fAngleLast  = (float)Math.tan(ANGLE_RIGHT);
		
		float arrAngle[] = new float[Rally.FIELDWIDTH * 2];
		
		// длины оснований трапеции трассы
		float fGlobalTrapUp   = m_iTrackWidth - m_iTrackHeight / fAngleFirst;
		float fGlobalTrapDown = m_iTrackWidth - m_iTrackHeight / fAngleLast;
		
		// расчет тангенсов оставшихся углов
		for (int i = 0; i < (Rally.FIELDWIDTH * 2); i++)
		{
			arrAngle[i] = m_iTrackHeight / (m_iTrackHeight / fAngleFirst - arrLane[i] *
				(fGlobalTrapDown - fGlobalTrapUp));
		}
		
		// очистка
		for (int y = 0 ; y < Rally.FIELDHEIGHT; y++)
		{
			for (int x = 0; x < Rally.FIELDWIDTH; x++)
			{
				m_arrTrack[y][x].reset();
			}
		}
		
		// отрисовка
		float fUpY   = 0;
		float fDownY = fTrackOneHeight; 
		
		for (int y = 0 ; y < Rally.FIELDHEIGHT; y++)
		{
			if ((0 != y) && (0 == y % 2))
			{
				fUpY   += fTrackOneStep * 2;
				fDownY += fTrackOneStep * 2;
			}
			
			// по две черточки на полоску
			for (int i = 0; i < 2; i++)
			{
				for (int x = 0; x < Rally.FIELDWIDTH; x++)
				{
					Path pTrack = m_arrTrack[y][x];
				
					pTrack.moveTo(arrLane[x * 2] * fGlobalTrapDown +
						(m_iTrackHeight - fUpY) / arrAngle[x * 2], fUpY);
					pTrack.lineTo(arrLane[x * 2] * fGlobalTrapDown +
						(m_iTrackHeight - fDownY) / arrAngle[x * 2], fDownY);
					pTrack.lineTo(arrLane[x * 2 + 1] * fGlobalTrapDown +
						(m_iTrackHeight - fDownY) / arrAngle[x * 2 + 1], fDownY);
					pTrack.lineTo(arrLane[x * 2 + 1] * fGlobalTrapDown +
						(m_iTrackHeight - fUpY) / arrAngle[x * 2 + 1], fUpY);
					pTrack.close();
				}
				
				fUpY   += fTrackOneStep;
				fDownY += fTrackOneStep;
			}

			fUpY   += fTrackOneStep * 2;
			fDownY += fTrackOneStep * 2;
		}

		// отрисовка боковых линий и бордюров
		int arrLineIndex[] = {TRACK_DELIMITER_INDEX_LEFT_LINE, TRACK_DELIMITER_INDEX_RIGHT_LINE};
		int arrBorderIndex[] = {TRACK_DELIMITER_INDEX_LEFT_BORDER, TRACK_DELIMITER_INDEX_RIGHT_BORDER};

		int   iCurrentIndex;
		float fAngleLineLeft;
		float fAngleLineRight;
		Path  pathTmp;

		for (int i = 0; i < 2; i++)
		{
			iCurrentIndex   = arrLineIndex[i];
			fAngleLineLeft  = m_iTrackHeight / (m_iTrackHeight / fAngleFirst -
				TRACK_DELIMITER[iCurrentIndex] * (fGlobalTrapDown - fGlobalTrapUp));
			fAngleLineRight = m_iTrackHeight / (m_iTrackHeight / fAngleFirst -
				TRACK_DELIMITER[iCurrentIndex + 1] * (fGlobalTrapDown - fGlobalTrapUp));
			pathTmp         = m_arrTrackLine[i];
			
			pathTmp.reset();
			pathTmp.moveTo(TRACK_DELIMITER[iCurrentIndex] *
				fGlobalTrapDown + (m_iTrackHeight) / fAngleLineLeft, 0);
			pathTmp.lineTo(TRACK_DELIMITER[iCurrentIndex] *
				fGlobalTrapDown, m_iTrackHeight);
			pathTmp.lineTo(TRACK_DELIMITER[iCurrentIndex + 1] *
				fGlobalTrapDown, m_iTrackHeight);
			pathTmp.lineTo(TRACK_DELIMITER[iCurrentIndex + 1] *
				fGlobalTrapDown + m_iTrackHeight / fAngleLineRight, 0);
			pathTmp.close();
			
			// отрисовка бордюров
			iCurrentIndex   = arrBorderIndex[i];
			fAngleLineLeft  = m_iTrackHeight / (m_iTrackHeight / fAngleFirst -
				TRACK_DELIMITER[iCurrentIndex] * (fGlobalTrapDown - fGlobalTrapUp));
			fAngleLineRight = m_iTrackHeight / (m_iTrackHeight / fAngleFirst -
				TRACK_DELIMITER[iCurrentIndex + 1] * (fGlobalTrapDown - fGlobalTrapUp));

			for (int y = 0; y < Rally.FIELDHEIGHT; y++)
			{
				pathTmp = m_arrTrackBorder[i][y];

				float iUpY = y * m_iTrackHeight / Rally.FIELDHEIGHT;
				float iDownY = (y + 1) * m_iTrackHeight / Rally.FIELDHEIGHT - m_iTrackHeight / 18;
				
				pathTmp.reset();
				
				pathTmp.moveTo(TRACK_DELIMITER[iCurrentIndex] * fGlobalTrapDown +
					(m_iTrackHeight - iUpY) / fAngleLineLeft, iUpY);
				pathTmp.lineTo(TRACK_DELIMITER[iCurrentIndex] * fGlobalTrapDown +
					(m_iTrackHeight - iDownY) / fAngleLineLeft, iDownY);
				pathTmp.lineTo(TRACK_DELIMITER[iCurrentIndex + 1] * fGlobalTrapDown +
					(m_iTrackHeight - iDownY) / fAngleLineRight, iDownY);
				pathTmp.lineTo(TRACK_DELIMITER[iCurrentIndex + 1] * fGlobalTrapDown +
					(m_iTrackHeight - iUpY) / fAngleLineRight, iUpY);
				pathTmp.close();
			}
		}
		
		// вычисление позиций машины
		for (int x = 0; x < Rally.FIELDWIDTH; x++)
		{
			m_ptCar[x].x = (int)(fGlobalTrapDown * arrLane[x * 2 + 1] - m_iCarWidth); 
			m_ptCar[x].y = m_iTrackHeight;
		}

		// размеры и позиция цифр (35x64) оригинал
		m_iNumberWidth  = (int)(m_iTrackHeight / fAngleFirst / 3.f / OUTPUT_NUMBERS_MAX);
		m_iNumberHeight = (int)((float)NUMBERHEIGHT / NUMBERWIDTH * m_iNumberWidth);
		
		m_iNumberDX   = m_iNumberWidth  / 2; 
		m_iNumberDY   = m_iNumberHeight / 2;
		m_iNumberStep = m_iNumberWidth  / 3;

		// расчет размеров и координат флага
		m_iFlagWidth  = (int)(FLAGWIDTH / 3.5f);
		m_iFlagHeight = (int)(FLAGHEIGHT / 3.5f);

		/* Позиция флага по высоте */
		int m_iFlagPosY = m_iHeight - m_iRectGameType - m_iFlagHeight;
		
		// отрисовка травы
		m_pathGrass.reset();

		m_pathGrass.moveTo(m_iTrackWidth + 1, 0);
		m_pathGrass.lineTo(m_iWidth, 0);
		m_pathGrass.lineTo(m_iWidth, m_iFlagPosY);
		m_pathGrass.lineTo(fGlobalTrapDown, m_iFlagPosY);
		m_pathGrass.close();
		
		m_pathGrass.moveTo(OUTPUT_NUMBERS_MAX * (m_iNumberWidth + 5), 0);
		m_pathGrass.lineTo(TRACK_DELIMITER[TRACK_DELIMITER_INDEX_LEFT_LINE] * fGlobalTrapDown +
			(m_iTrackHeight) / (m_iTrackHeight / (m_iTrackHeight / fAngleFirst -
				TRACK_DELIMITER[TRACK_DELIMITER_INDEX_LEFT_LINE] * (fGlobalTrapDown - fGlobalTrapUp))), 0);
		m_pathGrass.close();
		
		// загрузка битмапов
		loadBitmaps();
	}
	
	/**
	 * Загрузка/формирование битмапов.
	 */
	protected void loadBitmaps()
	{
		Drawable draw;
		Bitmap bitmap;
		Canvas canvas;
		
		// загрузка машинки
		final int[] arrCars = {R.drawable.car, R.drawable.car_trace};
		
		for (int i = 0; i < 2; i++)
		{
			bitmap = Bitmap.createBitmap(m_iCarWidth, m_iCarHeight, Bitmap.Config.ARGB_8888);
			canvas = new Canvas(bitmap);
			
			draw = m_res.getDrawable(arrCars[i]);
			draw.setBounds(0, 0, m_iCarWidth, m_iCarHeight);
			draw.draw(canvas);
			
			m_bmpCar[i] = bitmap;
		}
		
		// загрузка цифр
		final int[] arrNumber = {
			R.drawable.number_0, 
			R.drawable.number_1, 
			R.drawable.number_2, 
			R.drawable.number_3, 
			R.drawable.number_4, 
			R.drawable.number_5, 
			R.drawable.number_6, 
			R.drawable.number_7, 
			R.drawable.number_8, 
			R.drawable.number_9,
			R.drawable.number_colon
		};
				
		for (int i = 0; i <= 10; i++)
		{
			bitmap = Bitmap.createBitmap(m_iNumberWidth, m_iNumberHeight, Bitmap.Config.ARGB_8888);
			canvas = new Canvas(bitmap);

			draw = m_res.getDrawable(arrNumber[i]);

			draw.setBounds(0, 0, m_iNumberWidth, m_iNumberHeight);
			draw.draw(canvas);
			
			m_bmpNumbers[i] = bitmap;
		}
		
		// отрисовка иконок PM/AM
		TextPaint paintText12Hour = new TextPaint();
		paintText12Hour.setAntiAlias(true);
		paintText12Hour.setSubpixelText(true);
		paintText12Hour.setColor(m_res.getColor(R.color.rally_color_view_paint));
		
		Rect rectHour = new Rect();
		
		paintText12Hour.setTextSize(m_iNumberHeight);
		paintText12Hour.getTextBounds("AM", 0, 2, rectHour);
		
		paintText12Hour.setTextSize(m_iNumberHeight * (m_iNumberHeight / 2.0f / rectHour.height()));
		paintText12Hour.getTextBounds("AM", 0, 2, rectHour);

		bitmap = Bitmap.createBitmap(rectHour.width(), rectHour.height(), Bitmap.Config.ARGB_8888);
		canvas = new Canvas(bitmap);
		canvas.drawText("AM", 0, 2, 0, rectHour.height(), paintText12Hour);
		m_bmp12Hour[0] = bitmap;

		bitmap = Bitmap.createBitmap(rectHour.width(), rectHour.height(), Bitmap.Config.ARGB_8888);
		canvas = new Canvas(bitmap);
		canvas.drawText("PM", 0, 2, 0, rectHour.height(), paintText12Hour);
		m_bmp12Hour[1] = bitmap;
		
		////////////////////////////////////////////////////
		// отрисовка битмапов для вывода типа игры
		////////////////////////////////////////////////////

		// прямоугольник для вписывания текста
		Rect rectText = new Rect();

		// для получения ширины буквы
		float fWidth[] = new float[1];
		
		// текст
		String strGameA = m_activity.getString(R.string.rally_text_short_game_a);
		String strGameB = m_activity.getString(R.string.rally_text_short_game_b);
		
		// кисть для вывода текста
		TextPaint paintTextGameType = new TextPaint();

		paintTextGameType.setAntiAlias(true);
		paintTextGameType.setSubpixelText(true);
		paintTextGameType.setColor(m_res.getColor(R.color.rally_color_view_paint));
		paintTextGameType.setTypeface(Typeface.DEFAULT_BOLD);
		
		// кисть для рисования круга
		Paint paintCircleGameType = new Paint();

		paintCircleGameType.setColor(m_res.getColor(R.color.rally_color_view_bg));
		paintCircleGameType.setAntiAlias(true);
		
		// радиус круга
		float fCircleRadius = m_iRectGameType / 2.0f * 0.9f;

		// вычисление размеров текста для игры А
		paintTextGameType.setTextSize(m_iRectGameType);
		paintTextGameType.getTextBounds(strGameA, 0, strGameA.length(), rectText);

		paintTextGameType.setTextSize(m_iRectGameType *  fCircleRadius *
			2.0f / (float)Math.sqrt(rectText.width() * rectText.width() +
			rectText.height() * rectText.height()) * 0.9f);
		
		// отрисовка битмапа для игры А
		bitmap = Bitmap.createBitmap(m_iRectGameType, m_iRectGameType, Bitmap.Config.ARGB_8888);
		canvas = new Canvas(bitmap);
		
		canvas.drawColor(m_res.getColor(R.color.rally_color_view_paint));
		canvas.drawCircle(m_iRectGameType / 2, m_iRectGameType / 2, fCircleRadius, paintCircleGameType);

		paintTextGameType.getTextBounds(strGameA, 0, 1, rectText);
		paintTextGameType.getTextWidths(strGameA, 0, 1, fWidth);

		canvas.drawText(strGameA, 0, 1, (m_iRectGameType - fWidth[0]) / 2,
			(m_iRectGameType + rectText.height()) / 2, paintTextGameType);
		
		m_bmpGameType[0] = bitmap;
		
		// отрисовка битмапа для игры Б
		bitmap = Bitmap.createBitmap(m_iRectGameType, m_iRectGameType, Bitmap.Config.ARGB_8888);
		canvas = new Canvas(bitmap);
		
		canvas.drawColor(m_res.getColor(R.color.rally_color_view_paint));
		canvas.drawCircle(m_iRectGameType / 2, m_iRectGameType / 2, fCircleRadius, paintCircleGameType);

		paintTextGameType.getTextBounds(strGameB, 0, 1, rectText);
		paintTextGameType.getTextWidths(strGameB, 0, 1, fWidth);

		canvas.drawText(strGameB, 0, 1, (m_iRectGameType - fWidth[0]) / 2,
			(m_iRectGameType + rectText.height()) / 2, paintTextGameType);
		
		m_bmpGameType[1] = bitmap;
		
		// Битмап флага
		bitmap = Bitmap.createBitmap(m_iFlagWidth, m_iFlagHeight, Bitmap.Config.ARGB_8888);
		canvas = new Canvas(bitmap);
		
		draw = m_res.getDrawable(R.drawable.flag);

		draw.setBounds(0, 0, m_iFlagWidth, m_iFlagHeight);
		draw.draw(canvas);
	}

	/**
	 * Отрисовка игрового поля.
	 */
	@SuppressWarnings("deprecation")
	@Override
	protected void onDraw(Canvas canvas)
	{
		super.onDraw(canvas);
		
		if (0 == m_iWidth)
		{
			return;
		}
		
		// очистка поля
		canvas.drawColor(m_res.getColor(R.color.rally_color_view_bg));

		// TODO отрисовка травый
		// canvas.drawPath(m_pathGrass, m_paintGrass);

		// отрисовка поля
		for (int y = 0; y < Rally.FIELDHEIGHT; y++)
		{
			for (int x = 0; x < Rally.FIELDWIDTH; x++)
			{
				if (null == m_arrTrack[y][x])
				{
					continue;
				}

				if (0 == m_cRally.m_arrField[y][x])
				{
					continue;
				}
				
				canvas.drawPath(m_arrTrack[y][x], m_paintField);
			}
		}

		// отрисовка бордюров и боковых линий
		for (int i = 0; i < 2; i++)
		{
			canvas.drawPath(m_arrTrackLine[i], m_paintLine);

			for (int y = 0; y < Rally.FIELDHEIGHT; y++)
			{
				if (0 == m_cRally.m_arrBorders[y])
				{
					continue;
				}
				
				canvas.drawPath(m_arrTrackBorder[i][y], m_paintField);
			}
		}
		
		// отрисовка машины
		if (RallyActivity.GAME_MODE_LIFECOUNT == m_activity.m_iGameMode)
		{
			for (int x = 0; x < m_cRally.m_iLifeCount; x++)
			{
				if (x >= Rally.FIELDWIDTH)
				{
					break;
				}
				
				canvas.drawBitmap(m_bmpCar[0], m_ptCar[Rally.FIELDWIDTH - x - 1].x,
					m_ptCar[Rally.FIELDWIDTH - x - 1].y, null);
			}
		}
		else
		{
			if (RallyActivity.GAME_MODE_PLAY == m_activity.m_iGameMode)
			{
				canvas.drawBitmap(m_bmpCar[1], m_ptCar[m_cRally.m_iPlayerPos].x,
					m_ptCar[m_cRally.m_iPlayerPos].y, null);
			}
			else
			{
				canvas.drawBitmap(m_bmpCar[0], m_ptCar[m_cRally.m_iPlayerPos].x,
					m_ptCar[m_cRally.m_iPlayerPos].y, null);
			}
		}

		// отрисовка игры А или Б
		if (Rally.GAME_DEMO != m_cRally.m_iTypeGame)
		{
			if (Rally.GAME_A == m_cRally.m_iTypeGame)
			{
				canvas.drawBitmap(m_bmpGameType[0], m_iWidth - m_iRectGameType * 2 - 5,
					m_iHeight - m_iRectGameType, null);
			}
			else
			{
				canvas.drawBitmap(m_bmpGameType[1], m_iWidth - m_iRectGameType,
					m_iHeight - m_iRectGameType, null);
			}
		}
		
		// отрисовка количества очков
		int iOffset = OUTPUT_NUMBERS_MAX - 1;
		int iNumberView;
		int iNumber;

		if (Rally.GAME_DEMO != m_cRally.m_iTypeGame)
		{
			// вывод очков (или таймера отсчета)
			if ((RallyActivity.GAME_MODE_PREPLAY == m_activity.m_iGameMode) ||
					(RallyActivity.GAME_MODE_POSTPAUSE == m_activity.m_iGameMode))
			{
				iNumberView = m_activity.m_iCountdown;
			}
			else
			{
				iNumberView = m_cRally.m_iScore;
			}

			do 
			{
				iNumber = iNumberView % 10;
				iNumberView -= iNumber;
				iNumberView /= 10;
			
				canvas.drawBitmap(m_bmpNumbers[iNumber], m_iNumberDX +
					iOffset * (m_iNumberWidth + m_iNumberStep), m_iNumberDY, null);
			
				iOffset--;
			} while (iNumberView > 0);
		}
		else
		{
			// установка текущей даты
			m_dateCurrent.setTime(System.currentTimeMillis());
			
			// вывод времени
			iNumberView = m_dateCurrent.getMinutes();

			for (int i = 0; i < 2; i++)
			{
				iNumber = iNumberView % 10;
				iNumberView -= iNumber;
				iNumberView /= 10;

				canvas.drawBitmap(m_bmpNumbers[iNumber], m_iNumberDX +
					iOffset * (m_iNumberWidth + m_iNumberStep), m_iNumberDY, null);
				
				iOffset--;
			}

			canvas.drawBitmap(m_bmpNumbers[10], m_iNumberDX +
				iOffset * (m_iNumberWidth + m_iNumberStep), m_iNumberDY, null);
			iOffset--;

			iNumberView = m_dateCurrent.getHours();
			
			if (!m_bIs24Hour)
			{
				if (iNumberView >= 12)
				{
					iNumberView -= 12;
				}
				
				if (0 == iNumberView)
				{
					iNumberView = 12;
				}
			}
			
			for (int i = 0; i < 2; i++)
			{
				iNumber = iNumberView % 10;
				iNumberView -= iNumber;
				iNumberView /= 10;

				canvas.drawBitmap(m_bmpNumbers[iNumber], m_iNumberDX +
					iOffset * (m_iNumberWidth + m_iNumberStep), m_iNumberDY, null);
				
				iOffset--;
				
				// ведущий ноль выводить не надо (типа 09:59)
				if (0 == iNumberView)
				{
					break;
				}
			}

			// вывод AM/PM
			if (!m_bIs24Hour)
			{
				if (m_dateCurrent.getHours() < 12)
				{
					canvas.drawBitmap(m_bmp12Hour[0], m_iNumberDX +
						iOffset * (m_iNumberWidth + m_iNumberStep), m_iNumberDY, null);
				}
				else
				{
					canvas.drawBitmap(m_bmp12Hour[1], m_iNumberDX +
						iOffset * (m_iNumberWidth + m_iNumberStep), m_iNumberDY + m_iNumberHeight / 2, null);
				}
			}
		}
	}
}
