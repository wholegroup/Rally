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

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Rect;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

/** */
public class RallyActivity extends Activity
{
	/** Демонстрационный режим игры. */
	public final static int GAME_MODE_DEMO = 1;

	/** Режим вывода количества жизней. */
	public final static int GAME_MODE_LIFECOUNT = 2;
	
	/** Режим подготовки к игре - таймер. */
	public final static int GAME_MODE_PREPLAY = 3;
	
	/** Режим игры. */
	public final static int GAME_MODE_PLAY = 4;
	
	/** Пауза. */
	public final static int GAME_MODE_PAUSE = 5;

	/** Режим подготовки к игре после паузы. */
	public final static int GAME_MODE_POSTPAUSE = 6;
	
	/** Режим завершения игры. */
	public final static int GAME_MODE_GAMEOVER = 7; 

	/** Идентификатор меню 'игра А'. */
	private static final int MENU_GAME_A_ID = Menu.FIRST + 1;
	
	/** Идентификатор меню 'игра Б'. */
	private static final int MENU_GAME_B_ID = Menu.FIRST + 2;
	
	/** Идентификатор меню для включения/выключения звука. */
	private static final int MENU_GAME_SOUND_ID = Menu.FIRST + 3;

	/** Идентификатор меню 'очки'. */
	private static final int MENU_SCORE_ID = Menu.FIRST + 4;

	/** Таймер в ДЕМО режиме в мс. */
	private static final int TIMER_DEMO = 500;
	
	/** Таймер вывода количества жизней. */
	private static final int TIMER_LIFECOUNT = 1000;

	/** Таймер в режиме подготовки к старту. */
	private static final int TIMER_PREPLAY = 1000;
	
	/** Количество секунд для обратного отсчета. */
	private static final int COUNTDOWN = 3;
	
	/** Ширина рамки экрана. */
	private static final int FRAME_IMG_WIDTH = 586;
	
	/** Высота рамки экрана. */
	private static final int FRAME_IMG_HEIGHT = 432;
	
	/** Пропорции рамки экрана. */
	private static final float FRAME_IMG_COEF = FRAME_IMG_HEIGHT / (float)FRAME_IMG_WIDTH; 
	
	/** Ширина полоски для кнопок от ширины рамки в процентах. */
	private static final float BUTTON_WIDTH = 0.25f;

	/** Эстетическая высота полосы пустого места от высоты рамки в процентах. */
	private static final float FREESPACE_HEIGHT = 0.05f;
	
	/** Ширина игрового поля. */
	private static final int TRACK_VIEW_WIDTH = 468;
	
	/** Высота игрового поля. */
	private static final int TRACK_VIEW_HEIGHT = 278;
	
	/** Ширина круглой кнопки. */
	private static final int BUTTON_CIRCLE_WIDTH = 118;

	/** Высота круглой кнопки. */
	private static final int BUTTON_CIRCLE_HEIGHT = 76;
	
	/** Ширина экрана. */
	public int m_iWidth = 0;
	
	/** Высота экрана. */
	public int m_iHeight = 0;
	
	/** Указатель на класс игры. */
	private Rally m_cGame;
	
	/** Режим игры. */
	public int m_iGameMode;
	
	/** Обратный отсчет. */
	public int m_iCountdown;
	
	/** Фоновое изображение. */
	private View m_viewBackground;
	
	/** Окно вывода игры. */
	private RallyView m_viewRally;
	
	/** Кнопка влево. */
	private ImageButton m_btnLeft; 
	
	/** Кнопка вправо. */
	private ImageButton m_btnRight; 
	
	/** Кнопка игра А. */
	private ImageButton m_btnGameA; 

	/** Кнопка игра B. */
	private ImageButton m_btnGameB; 
	
	/** Кнопка меню. */
	private ImageButton m_btnMenu; 
	
	/** Слой "ПАУЗА". */
	private View m_viewPause;
	
	/** Слой "КОНЕЦ ИГРЫ". */
	private View m_viewStop;
	
	/** Звук. */
	private boolean m_bSound; 
	
	/** Звук обратного отсчета. */
	private MediaPlayer m_mpTimer;

	/** Звук проигрыша. */
	private MediaPlayer m_mpDead;
	
	/** Звук движения вперед. */
	private MediaPlayer m_mpStep;
	
	/** Класс для создания анимации. */
	private RefreshHandler m_RedrawHandler = new RefreshHandler();
	
	class RefreshHandler extends Handler
	{
		@Override
		public void handleMessage(Message msg)
		{
			RallyActivity.this.refresh();
		}

		public void sleep(long delayMillis)
		{
			this.removeMessages(0);

			sendMessageDelayed(obtainMessage(0), delayMillis);
		}
		
		public void stop()
		{
			this.removeMessages(0);
		}
	}
	
	/**
	 * Создание активности.
	 */
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		// установка видимого слоя
		setContentView(R.layout.rally);
		
		// игра
		m_cGame = new Rally();
		
		// при старте игры устанавливаем демо режим 
		m_iGameMode = GAME_MODE_DEMO;
		
		// ссылка на фоновое изображение
		m_viewBackground = findViewById(R.id.rally_layout_image);
		
		// ссылка на окно для вывода игры
		m_viewRally = (RallyView)findViewById(R.id.rally_view);

		m_viewRally.m_cRally = m_cGame;

		// кнопка влево
		m_btnLeft = (ImageButton)findViewById(R.id.rally_btn_left);
		
		m_btnLeft.setOnClickListener(new OnClickListener()
			{
				public void onClick(View view)
				{
					moveLeft();
				}
			}
		);
		
		// кнопка вправо
		m_btnRight = (ImageButton)findViewById(R.id.rally_btn_right);
		
		m_btnRight.setOnClickListener(new OnClickListener()
			{
				public void onClick(View view)
				{
					moveRight();
				}
			}
		);
		
		// кнопка игра А
		m_btnGameA = (ImageButton)findViewById(R.id.rally_btn_game_a);
		
		m_btnGameA.setOnClickListener(new OnClickListener()
			{
				public void onClick(View view)
				{
					startGame(Rally.GAME_A);
				}
			}
		);
		
		// кнопка игра B
		m_btnGameB = (ImageButton)findViewById(R.id.rally_btn_game_b);
		
		m_btnGameB.setOnClickListener(new OnClickListener()
			{
				public void onClick(View view)
				{
					startGame(Rally.GAME_B);
				}
			}
		);
		
		// кнопка меню
		m_btnMenu = (ImageButton)findViewById(R.id.rally_btn_menu);
		
		m_btnMenu.setOnClickListener(new OnClickListener()
			{
				public void onClick(View view)
				{
					getWindow().openPanel(Window.FEATURE_OPTIONS_PANEL, null);
				}
			}
		);
		
		// слой "ПАУЗА"
		m_viewPause = findViewById(R.id.rally_pause_layout);
		
		findViewById(R.id.rally_pause_button).setOnClickListener(new OnClickListener()
			{
				public void onClick(View view)
				{
					m_viewPause.setVisibility(View.INVISIBLE);
					
					m_iGameMode  = GAME_MODE_POSTPAUSE;
					m_iCountdown = COUNTDOWN;

					m_viewRally.invalidate();
					
					startTimer();
				}
			}
		);

		// слой "КОНЕЦ ИГРЫ"
		m_viewStop= findViewById(R.id.rally_gameover_layout);

		findViewById(R.id.rally_gameover_button_next).setOnClickListener(new OnClickListener()
			{
				public void onClick(View view)
				{
					m_viewStop.setVisibility(View.INVISIBLE);

					Intent intent = new Intent(RallyActivity.this, ScoreActivity.class);

					intent.putExtra(getString(R.string.score_parameter_score), m_cGame.m_iScore);
					intent.putExtra(getString(R.string.score_parameter_type), m_cGame.m_iTypeGame);

					startActivity(intent);

					//
					m_iGameMode = GAME_MODE_DEMO;

					m_cGame.startGame(Rally.GAME_DEMO);

					startTimer();
				}
			}
		);
		
		// инициализация звука
   	SharedPreferences settings = getSharedPreferences(getString(R.string.preferences_id), 0);

   	m_bSound = settings.getBoolean(getString(R.string.preferences_sound), true);
		
		if (m_bSound)
		{
			initSound();
		}
		
		// инициализируем демо режим
		m_iGameMode = GAME_MODE_DEMO;

		m_cGame.startGame(Rally.GAME_DEMO);

		// восстанавливаем состояние игры
		if (null != savedInstanceState)
		{
			m_iGameMode = savedInstanceState.getInt(getString(R.string.rally_parameter_mode));
			
			if (GAME_MODE_DEMO != m_iGameMode)
			{
				m_cGame.fromJSON(savedInstanceState.getString(getString(
					R.string.rally_parameter_instance)));
				
				if (GAME_MODE_GAMEOVER == m_iGameMode)
				{
					visibleStop();
				}
				else
				{
					m_viewPause.setVisibility(View.VISIBLE);

					findViewById(R.id.rally_pause_button).requestFocus();
				}
			}
		}

		startTimer();
	}
	
	/**
	 * Создание пунктов меню.
	 * - старт
	 * - очки
	 * - настройки
	 * - выход
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		menu.add(0, MENU_GAME_A_ID, 0, R.string.rally_menu_game_a)
			.setIcon(R.drawable.menu_a);
		menu.add(0, MENU_GAME_B_ID, 0, R.string.rally_menu_game_b)
			.setIcon(R.drawable.menu_b);
		
		if (m_bSound)
		{
			menu.add(0, MENU_GAME_SOUND_ID, 0, R.string.rally_menu_sound_off).
				setIcon(R.drawable.menu_sound_off);
		}
		else
		{
			menu.add(0, MENU_GAME_SOUND_ID, 0, R.string.rally_menu_sound_on).
				setIcon(R.drawable.menu_sound_on);
		}

		menu.add(0, MENU_SCORE_ID, 0, R.string.rally_menu_score)
			.setIcon(R.drawable.menu_score);

		return super.onCreateOptionsMenu(menu);
	}

	/**
	 * Обработка меню.
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch (item.getItemId())
		{
			// Старт игры А
			case MENU_GAME_A_ID:
			{
				startGame(Rally.GAME_A);

				break;
			}
			
			// Старт игры Б
			case MENU_GAME_B_ID:
			{
				startGame(Rally.GAME_B);

				break;
			}

			case MENU_GAME_SOUND_ID:
			{
				m_bSound = !m_bSound;

				if (m_bSound)
				{
					item.setTitle(getString(R.string.rally_menu_sound_off))
						.setIcon(R.drawable.menu_sound_off);

					initSound();
				}
				else
				{
					item.setTitle(getString(R.string.rally_menu_sound_on))
						.setIcon(R.drawable.menu_sound_on);
				}

				SharedPreferences settings = getSharedPreferences(getString(R.string.preferences_id), 0);

				SharedPreferences.Editor editor = settings.edit();

				editor.putBoolean(getString(R.string.preferences_sound), m_bSound);
				editor.commit();

				break;
			}
				
			// очки
			case MENU_SCORE_ID:
			{
				startActivity(new Intent(RallyActivity.this, ScoreActivity.class));

				break;
			}

			default:
				return super.onOptionsItemSelected(item);
		}

		return true;
	}

	/** 
	 * Обработчик нажатия клавиш.
	 */
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event)
	{
		switch (keyCode)
		{
			// влево
			case KeyEvent.KEYCODE_DPAD_LEFT:
			case KeyEvent.KEYCODE_Q:
			{
				moveLeft();

				return true;
			}

			// вправо
			case KeyEvent.KEYCODE_DPAD_RIGHT:
			case KeyEvent.KEYCODE_P:
			{
				moveRight();

				return true;
			}

			default:
				break;
		}

		return super.onKeyDown(keyCode, event);
	}
	
	/**
	 * Установка активности на паузу.
	 * - установка игры на паузу
	 */
	@Override
	public void onPause()
	{
		super.onPause();

		// останавливаем игру
		stopTimer();
		
		// завершаем выполнение функции, если приложение завершает свою работу
		if (isFinishing())
		{
			return;
		}

		// выводим слой паузы
		if (GAME_MODE_PAUSE == m_iGameMode)
		{
			m_viewPause.setVisibility(View.VISIBLE);

			findViewById(R.id.rally_pause_button).requestFocus();
		}
	}
	
	
	/**
	 * Возобновление работы активности.
	 */
	@Override
	public void onResume()
	{
		super.onResume();
		
		startTimer();
	}
	
	/**
	 * Сохранение состояния игры.
	 */
	@Override
	public void onSaveInstanceState(Bundle outState)
	{
		if (GAME_MODE_DEMO != m_iGameMode)
		{
			outState.putString(getString(R.string.rally_parameter_instance), m_cGame.toJSON());
			
			// если во время игры было инициировано сохранение состояния
			// => игру следует поставить в режим паузы
			if ((GAME_MODE_PLAY == m_iGameMode) || (GAME_MODE_PREPLAY == m_iGameMode))
			{
				m_iGameMode = GAME_MODE_PAUSE;
			}
		}

		outState.putInt(getString(R.string.rally_parameter_mode), m_iGameMode);

		super.onSaveInstanceState(outState);
	}
	
	/**
	 * Обработка изменения фокуса.
	 * - получение размеров рабочего окна
	 * - расчет координат расположения контролов
	 */
	@Override
	public void onWindowFocusChanged(boolean hasFocus) 
	{
		super.onWindowFocusChanged(hasFocus);

		// расчет координат расположения кнопок
		if ((0 == m_iWidth) || (0 == m_iHeight))
		{
			m_iWidth  = m_viewBackground.getWidth();
			m_iHeight = m_viewBackground.getHeight();
			
			setControlPosition();
		}

		// установка фокуса на кнопку Resume
		if (View.VISIBLE == m_viewPause.getVisibility())
		{
			findViewById(R.id.rally_pause_button).requestFocus();
		}
	}

	/**
	 * Установка размеров и позиций контролов игры.
	 * - фоновое изображение
	 * - кнопки
	 * - надписи  
	 */
	private void setControlPosition()
	{
		// реальное соотношение сторон экрана
		float fRealCoef = m_iHeight / (float)m_iWidth;

		// оптимальное соотношение сторон экрана
		float fOptimalWidth  = FRAME_IMG_WIDTH * (1.0f + 2 * BUTTON_WIDTH);  
		float fOptimalHeight = FRAME_IMG_HEIGHT * (1.0f + 2 * FREESPACE_HEIGHT);
		float fOptimalCoef   = fOptimalHeight / fOptimalWidth;
		
		// установка размеров рамки 
		int iFrameWidth;
		int iFrameHeight;
		
		if (fOptimalCoef < fRealCoef)
		{
			iFrameWidth  = (int)(m_iWidth / (1.0f + 2 * BUTTON_WIDTH));
			iFrameHeight = (int)(FRAME_IMG_COEF * iFrameWidth);
		}
		else
		{
			iFrameHeight = (int)(m_iHeight / (1.0f + 2 * FREESPACE_HEIGHT));
			iFrameWidth  = (int)(iFrameHeight / FRAME_IMG_COEF);
		}
		
		FrameLayout.LayoutParams lpFrame = new FrameLayout.LayoutParams(iFrameWidth, iFrameHeight);
		lpFrame.gravity = Gravity.CENTER;
		
		findViewById(R.id.rally_layout_frame).setLayoutParams(lpFrame);
		
		// установка размеров вывода трассы
		float fScaleCoef   = iFrameWidth / (float)FRAME_IMG_WIDTH;
		int   iTrackWidth  = (int)(TRACK_VIEW_WIDTH * fScaleCoef);
		int   iTrackHeight = (int)(TRACK_VIEW_HEIGHT * fScaleCoef);
		
		FrameLayout.LayoutParams lpRally = new FrameLayout.LayoutParams(iTrackWidth, iTrackHeight);
		lpRally.gravity = Gravity.CENTER;

		m_viewRally.setLayoutParams(lpRally);
		m_viewRally.setSize(iTrackWidth, iTrackHeight);
		
		// ширина поля для расположения кнопок
		float fBtnWidth = iFrameWidth * BUTTON_WIDTH;
		
		// размеры круглых кнопок
		int iCircleBtnWidth  = (int)fBtnWidth;
		int iCircleBtnHeight = (int)(BUTTON_CIRCLE_HEIGHT *
			(iCircleBtnWidth / (float)BUTTON_CIRCLE_WIDTH));
		
		// кнопка влево
		LinearLayout.LayoutParams lpLeft = new LinearLayout.LayoutParams(
			iCircleBtnWidth, iCircleBtnHeight);
		lpLeft.topMargin  = m_iHeight - iCircleBtnHeight;
		lpLeft.leftMargin = 0;
		
		if (((m_iHeight - iFrameHeight) / 2) < (iCircleBtnHeight / 2))
		{
			lpLeft.topMargin -= (m_iHeight - iFrameHeight) / 2;
		}
		else
		{
			lpLeft.topMargin -= iCircleBtnHeight / 2;
		}

		m_btnLeft.setLayoutParams(lpLeft);

		// кнопка влево
		LinearLayout.LayoutParams lpRight = new LinearLayout.LayoutParams(
			iCircleBtnWidth, iCircleBtnHeight);
		lpRight.topMargin  = lpLeft.topMargin;
		lpRight.leftMargin = m_iWidth - iCircleBtnWidth;

		m_btnRight.setLayoutParams(lpRight);
		
		// размеры прямоугольных кнопок
		int iRectBtnWidth = 32;
		int iRectBtnHeight = 24;
		
		// кнопка игра А
		LinearLayout.LayoutParams lpGameA = new LinearLayout.LayoutParams(
			iRectBtnWidth, iRectBtnHeight);
		lpGameA.topMargin  = (m_iHeight - iFrameHeight) / 2;
		lpGameA.leftMargin = m_iWidth - (iCircleBtnWidth + iRectBtnWidth) /2;

		m_btnGameA.setLayoutParams(lpGameA);
		
		// кнопка игра Б
		LinearLayout.LayoutParams lpGameB = new LinearLayout.LayoutParams(
			iRectBtnWidth, iRectBtnHeight);
		lpGameB.topMargin  = lpGameA.topMargin + iRectBtnHeight * 2;
		lpGameB.leftMargin = lpGameA.leftMargin;

		m_btnGameB.setLayoutParams(lpGameB);
		
		// кнопка меню
		LinearLayout.LayoutParams lpMenu = new LinearLayout.LayoutParams(
			iRectBtnWidth, iRectBtnHeight);
		lpMenu.topMargin  = lpGameB.topMargin + iRectBtnHeight * 2;
		lpMenu.leftMargin = lpGameA.leftMargin;

		m_btnMenu.setLayoutParams(lpMenu);
		
		// вычисление самой длинной надписи (игра А, игра Б, меню)
		Rect rcTextMax = new Rect();
		Rect rcTmp     = new Rect();
		
		TextView tvGameA = (TextView)findViewById(R.id.rally_text_game_a);

		tvGameA.getDrawingRect(rcTextMax);

		TextView tvGameB = (TextView)findViewById(R.id.rally_text_game_b);

		tvGameB.getDrawingRect(rcTmp);
		
		if (rcTmp.width() > rcTextMax.width())
		{
			rcTextMax.set(rcTmp);
		}
		
		TextView tvMenu = (TextView)findViewById(R.id.rally_text_menu);

		tvMenu.getDrawingRect(rcTmp);
		
		if (rcTmp.width() > rcTextMax.width())
		{
			rcTextMax.set(rcTmp);
		}
		
		// вычисление и установка нового размера шрифта для надписей
		float fOldSize  = tvGameA.getTextSize();
		float fSizeCoef = iRectBtnWidth * 1.5f / rcTextMax.width();
		float fNewSize  = fOldSize * fSizeCoef;
		
		tvGameA.setTextSize(fNewSize);
		tvGameB.setTextSize(fNewSize);
		tvMenu.setTextSize(fNewSize);
		
		// установка положения надписей
		tvGameA.getDrawingRect(rcTmp);

		LinearLayout.LayoutParams lpTextGameA = new LinearLayout.LayoutParams(
			LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
		lpTextGameA.leftMargin = lpGameA.leftMargin +
			(int)((iRectBtnWidth- rcTmp.width() * fSizeCoef) / 2.0f);
		lpTextGameA.topMargin  = lpGameA.topMargin + iRectBtnHeight;
		
		tvGameA.setLayoutParams(lpTextGameA);

		tvGameB.getDrawingRect(rcTmp);

		LinearLayout.LayoutParams lpTextGameB = new LinearLayout.LayoutParams(
			LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
		lpTextGameB.leftMargin = lpGameB.leftMargin +
			(int)((iRectBtnWidth- rcTmp.width() * fSizeCoef) / 2.0f);
		lpTextGameB.topMargin  = lpGameB.topMargin + iRectBtnHeight;
		
		tvGameB.setLayoutParams(lpTextGameB);

		tvMenu.getDrawingRect(rcTmp);

		LinearLayout.LayoutParams lpTextMenu = new LinearLayout.LayoutParams(
			LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
		lpTextMenu.leftMargin = lpMenu.leftMargin +
			(int)((iRectBtnWidth- rcTmp.width() * fSizeCoef) / 2.0f);
		lpTextMenu.topMargin  = lpMenu.topMargin + iRectBtnHeight;
		
		tvMenu.setLayoutParams(lpTextMenu);
	}

	/**
	 * Движение влево.
	 */
	private void moveLeft()
	{
		if ((null == m_cGame) || (m_iGameMode != GAME_MODE_PLAY))
		{
			return;
		}
		
		if (m_cGame.moveLeft())
		{
			m_viewRally.invalidate();
		}
	}
	
	/**
	 * Движение вправо.
	 */
	private void moveRight()
	{
		if ((null == m_cGame) || (m_iGameMode != GAME_MODE_PLAY))
		{
			return;
		}
		
		if (m_cGame.moveRight())
		{
			m_viewRally.invalidate();
		}
	}
	
	/**
	 * Старт игры.
	 */
	private void startGame(int iTypeGame)
	{
		if ((null == m_cGame) || (m_iGameMode != GAME_MODE_DEMO))
		{
			return;
		}

		stopTimer();
		
		m_iGameMode  = GAME_MODE_LIFECOUNT;
		
		m_cGame.startGame(iTypeGame);
		
		m_viewRally.invalidate();
		
		startTimer();
	}
	
	/**
	 * Обработка событий по таймеру.
	 */
	private void refresh()
	{
		switch (m_iGameMode)
		{
			//
			case GAME_MODE_DEMO:
			{
				m_cGame.moveForward();
				m_viewRally.invalidate();
				m_RedrawHandler.sleep(TIMER_DEMO);

				break;
			}

			//
			case GAME_MODE_LIFECOUNT:
			{
				m_iGameMode  = GAME_MODE_PREPLAY;
				m_iCountdown = COUNTDOWN;

				m_viewRally.invalidate();
				m_RedrawHandler.sleep(TIMER_PREPLAY);

				break;
			}

			//
			case GAME_MODE_PREPLAY:
			case GAME_MODE_POSTPAUSE:
			{
				if (m_bSound && (null != m_mpTimer))
				{
					if (0 != m_mpTimer.getCurrentPosition())
					{
						m_mpTimer.seekTo(0);
					}
					
					m_mpTimer.start();
				}
				
				m_iCountdown--;
				
				if (0 < m_iCountdown)
				{
					m_RedrawHandler.sleep(TIMER_PREPLAY);
				}
				else
				{
					m_iGameMode  = GAME_MODE_PLAY;
					
					m_RedrawHandler.sleep(m_cGame.m_iSpeedMS);
				}

				m_viewRally.invalidate();
				
				break;
			}

			//
			case GAME_MODE_PLAY:
			{
				if (!m_cGame.moveForward())
				{
					// включение вибры на 0.5 секунд
					Vibrator vibrator = (Vibrator)getSystemService(Context.VIBRATOR_SERVICE);
					vibrator.vibrate(500);
					
					if (m_bSound && (null != m_mpDead))
					{
						if (0 != m_mpDead.getCurrentPosition())
						{
							m_mpDead.seekTo(0);
						}
						
						m_mpDead.start();

						try
						{ 
							Thread.sleep(m_mpDead.getDuration());
						} catch(InterruptedException ignored){ }
					}
					
					if (m_cGame.decLife())
					{
						m_iGameMode  = GAME_MODE_LIFECOUNT;

						m_RedrawHandler.sleep(TIMER_LIFECOUNT);
						m_viewRally.invalidate();
					}
					else
					{
						visibleStop();
					}
					
					break;
				}
				
				if (m_bSound && (null != m_mpStep))
				{
					if (0 != m_mpStep.getCurrentPosition())
					{
						m_mpStep.seekTo(0);
					}
					
					m_mpStep.start();
				}
				
				m_viewRally.invalidate();

				m_RedrawHandler.sleep(m_cGame.m_iSpeedMS);
				
				break;
			}

			//
			case GAME_MODE_PAUSE:
			case GAME_MODE_GAMEOVER:
			{
				break;
			}
			
			default:
				break;
		}

	}

	/**
	 * Запуск таймера.
	 */
	private void startTimer()
	{
		switch (m_iGameMode)
		{
			//
			case GAME_MODE_DEMO:
			{
				m_RedrawHandler.sleep(TIMER_DEMO);

				break;
			}

			//
			case GAME_MODE_LIFECOUNT:
			{
				m_RedrawHandler.sleep(TIMER_LIFECOUNT);

				break;
			}

			//
			case GAME_MODE_PREPLAY:
			case GAME_MODE_POSTPAUSE:
			{
				m_RedrawHandler.sleep(TIMER_PREPLAY);

				break;
			}

			//
			case GAME_MODE_PLAY:
			{
				// FIXME паузу точно определить
				m_RedrawHandler.sleep(m_cGame.m_iSpeedMS);

				break;
			}

			//
			case GAME_MODE_PAUSE:
			case GAME_MODE_GAMEOVER:
			{
				break;
			}
			
			default:
				break;
		}
	}
	
	/**
	 * Остановка таймера.
	 */
	private void stopTimer()
	{
		m_RedrawHandler.stop();
	}

	/**
	 * Выводит на экран слой окончания игры.
	 */
	private void visibleStop()
	{
		TextView textTmp;
		
		textTmp = (TextView)findViewById(R.id.rally_gameover_score);

		textTmp.setText(getString(R.string.rally_gameover_score) + ": " +
			Integer.toString(m_cGame.m_iScore));

		m_viewStop.setVisibility(View.VISIBLE);

		findViewById(R.id.rally_gameover_button_next).requestFocus();
	}
	
	/**
	 * Уничтожение активности.
	 */
	@Override
	public void onDestroy()
	{
		if (null != m_mpDead)
		{
			m_mpDead.release();

			m_mpDead = null;
		}

		super.onDestroy();
	}

	/**
	 * Инициализация звуковых файлов.
	 */
	private void initSound()
	{
		if (null == m_mpDead)
		{
			m_mpDead = MediaPlayer.create(this, R.raw.end);
		}
		
		if (null == m_mpTimer)
		{
			m_mpTimer = MediaPlayer.create(this, R.raw.preplay);
		}
		
		if (null == m_mpStep)
		{
			m_mpStep = MediaPlayer.create(this, R.raw.step);
		}
	}
}
