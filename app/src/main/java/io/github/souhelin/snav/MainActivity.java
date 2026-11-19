package io.github.souhelin.snav;

import android.animation.*;
import android.app.*;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.*;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.*;
import android.graphics.*;
import android.graphics.Typeface;
import android.graphics.drawable.*;
import android.media.*;
import android.net.*;
import android.net.Uri;
import android.os.*;
import android.text.*;
import android.text.style.*;
import android.util.*;
import android.view.*;
import android.view.View;
import android.view.View.*;
import android.view.animation.*;
import android.webkit.*;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.*;
import com.google.android.material.materialswitch.MaterialSwitch;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import java.io.*;
import java.text.*;
import java.util.*;
import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.*;
import org.json.*;

public class MainActivity extends androidx.appcompat.app.AppCompatActivity {
	
	private Timer _timer = new Timer();
	
	private double toque = 0;
	private boolean allowInsecureNavigationOnce = false;
	private PermissionRequest pendingWebPermissionRequest;
	private String pendingGeolocationOrigin;
	private GeolocationPermissions.Callback pendingGeolocationCallback;
	
	private LinearLayout linear1;
	private LinearLayout linear_c;
	private LinearLayout linear82;
	private LinearLayout tela1;
	private LinearLayout tela2;
	private LinearLayout tela3;
	private TextView text_icon;
	private LinearLayout linear_barra_pesquisa;
	private EditText edittext1;
	private LinearLayout linear20;
	private LinearLayout linear_tela2_base;
	private LinearLayout linear_b;
	private LinearLayout vazio;
	private WebView webview;
	private TextView text_barra;
	private ImageButton button_site_info;
	private ProgressBar prog;
	private TextView text1;
	private TextView text2;
	private ScrollView vscroll1;
	private LinearLayout linear33;
	private LinearLayout linear_tela3_base;
	private LinearLayout linearmenu2;
	private LinearLayout linear_escurecer;
	private LinearLayout linear_JavaScript;
	private LinearLayout linear_limpar_dados2;
	private HorizontalScrollView hscroll1;
	private LinearLayout linear25;
	private LinearLayout linear_copiar_URL;
	private LinearLayout linear_tradutor_google;
	private LinearLayout linear_modo_leitura;
	private LinearLayout linear_abrir_no_navegador;
	private ImageView imageview57;
	private TextView text14;
	private ImageView imageview63;
	private TextView text19;
	private ImageView imageview92;
	private TextView text7;
	private ImageView imageview58;
	private TextView text15;
	private MaterialSwitch checkbox_brilho;
	private MaterialSwitch checkbox_javascript2;
	private TextView text5;
	private TextView b1;
	private TextView b2;
	
	private Intent intent = new Intent();
	private SharedPreferences Dados;
	private AlertDialog.Builder Limpar_Dialog;
	private TimerTask time;
	
	@Override
	protected void onCreate(Bundle _savedInstanceState) {
		super.onCreate(_savedInstanceState);
		setContentView(R.layout.main);
		startDiagnostics();
		initialize(_savedInstanceState);
		initializeLogic();
		addDiagnosticMenuAction();
		showPendingCrashReport();
	}
	
	private void initialize(Bundle _savedInstanceState) {
		linear1 = findViewById(R.id.linear1);
		linear_c = findViewById(R.id.linear_c);
		linear82 = findViewById(R.id.linear82);
		tela1 = findViewById(R.id.tela1);
		tela2 = findViewById(R.id.tela2);
		tela3 = findViewById(R.id.tela3);
		text_icon = findViewById(R.id.text_icon);
		linear_barra_pesquisa = findViewById(R.id.linear_barra_pesquisa);
		edittext1 = findViewById(R.id.edittext1);
		linear20 = findViewById(R.id.linear20);
		linear_tela2_base = findViewById(R.id.linear_tela2_base);
		linear_b = findViewById(R.id.linear_b);
		vazio = findViewById(R.id.vazio);
		webview = findViewById(R.id.webview);
		webview.getSettings().setJavaScriptEnabled(true);
		webview.getSettings().setSupportZoom(true);
		text_barra = findViewById(R.id.text_barra);
		button_site_info = findViewById(R.id.button_site_info);
		prog = findViewById(R.id.prog);
		text1 = findViewById(R.id.text1);
		text2 = findViewById(R.id.text2);
		vscroll1 = findViewById(R.id.vscroll1);
		linear33 = findViewById(R.id.linear33);
		linear_tela3_base = findViewById(R.id.linear_tela3_base);
		linearmenu2 = findViewById(R.id.linearmenu2);
		linear_escurecer = findViewById(R.id.linear_escurecer);
		linear_JavaScript = findViewById(R.id.linear_JavaScript);
		linear_limpar_dados2 = findViewById(R.id.linear_limpar_dados2);
		hscroll1 = findViewById(R.id.hscroll1);
		linear25 = findViewById(R.id.linear25);
		linear_copiar_URL = findViewById(R.id.linear_copiar_URL);
		linear_tradutor_google = findViewById(R.id.linear_tradutor_google);
		linear_modo_leitura = findViewById(R.id.linear_modo_leitura);
		linear_abrir_no_navegador = findViewById(R.id.linear_abrir_no_navegador);
		imageview57 = findViewById(R.id.imageview57);
		text14 = findViewById(R.id.text14);
		imageview63 = findViewById(R.id.imageview63);
		text19 = findViewById(R.id.text19);
		imageview92 = findViewById(R.id.imageview92);
		text7 = findViewById(R.id.text7);
		imageview58 = findViewById(R.id.imageview58);
		text15 = findViewById(R.id.text15);
		checkbox_brilho = findViewById(R.id.checkbox_brilho);
		checkbox_javascript2 = findViewById(R.id.checkbox_javascript2);
		text5 = findViewById(R.id.text5);
		b1 = findViewById(R.id.b1);
		b2 = findViewById(R.id.b2);
		Dados = getSharedPreferences("Dados", Activity.MODE_PRIVATE);
		Limpar_Dialog = new AlertDialog.Builder(this);
		
		linear_barra_pesquisa.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View _view) {
				openSearchScreen();
			}
		});

		ImageView searchAction = findViewById(R.id.image_search_action);
		if (searchAction != null) {
			searchAction.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View view) {
					submitSearch();
				}
			});
		}
		
		linear_b.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View _view) {
				openSearchScreen();
			}
		});
		
		webview.setWebViewClient(new WebViewClient() {
			@Override
			public void onPageStarted(WebView view, String url, Bitmap favicon) {
				logDiagnostic("Navegação iniciada: " + safeHostForDiagnostics(url));
				super.onPageStarted(view, url, favicon);
			}

			@Override
			public void onPageFinished(WebView view, String url) {
				logDiagnostic("Navegação concluída: " + safeHostForDiagnostics(url));
				super.onPageFinished(view, url);
			}

			@Override
			public void onReceivedError(WebView view, int errorCode,
				String description, String failingUrl) {
				logDiagnostic("Erro de carregamento WebView: código " + errorCode
					+ "; host=" + safeHostForDiagnostics(failingUrl));
				super.onReceivedError(view, errorCode, description, failingUrl);
			}

			@Override
			public void onReceivedSslError(WebView view, SslErrorHandler handler,
				android.net.http.SslError error) {
				logDiagnostic("Erro de certificado TLS: " + (error == null ? "desconhecido"
					: error.getPrimaryError()) + "; host="
					+ safeHostForDiagnostics(error == null ? null : error.getUrl()));
				handler.cancel();
			}
		});
		
		button_site_info.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View _view) {
				showSiteInfoBottomSheet();
			}
		});
		
		text2.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View _view) {
				openSearchScreen();
			}
		});
		
		linear_JavaScript.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View _view) {
				
			}
		});
		
		linear_limpar_dados2.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View _view) {
				Limpar_Dialog.setTitle("Limpar Dados de Navegação");
				Limpar_Dialog.setMessage("Você deseja limpar todos os dados de navegação?");
				Limpar_Dialog.setPositiveButton("SIM", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface _dialog, int _which) {
						webview.clearCache(true);
						webview.clearHistory();
					}
				});
				Limpar_Dialog.setNegativeButton("NÃO", null);
				
				// Cria e exibe
				final AlertDialog d = Limpar_Dialog.create();
				d.show();
				
				// Customização do Fundo (Cor 34495E e Raio 20)
				android.graphics.drawable.GradientDrawable gd = new android.graphics.drawable.GradientDrawable();
				gd.setColor(0xFF34495E);
				gd.setCornerRadius(20);
				d.getWindow().setBackgroundDrawable(gd);
				
				// Escurecer fundo da tela (Dim)
				d.getWindow().setDimAmount(0.7f);
				
				// Cores dos Botões (Cor 9E9E9E)
				d.getButton(DialogInterface.BUTTON_POSITIVE).setTextColor(0xFF9E9E9E);
				d.getButton(DialogInterface.BUTTON_NEGATIVE).setTextColor(0xFF9E9E9E);
				
				// Ajuste da cor do Título (Branco)
				int titleId = getResources().getIdentifier("alertTitle", "id", "android");
				if (titleId > 0) {
					android.widget.TextView tvTitle = (android.widget.TextView) d.findViewById(titleId);
					if (tvTitle != null) {
						tvTitle.setTextColor(android.graphics.Color.WHITE);
					}
				}
				
				// Ajuste da cor da Mensagem (Branco)
				android.widget.TextView tvMsg = (android.widget.TextView) d.findViewById(android.R.id.message);
				if (tvMsg != null) {
					tvMsg.setTextColor(android.graphics.Color.WHITE);
				}
				
			}
		});
		
		linear_copiar_URL.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View _view) {
				SketchwareUtil.showMessage(getApplicationContext(), "Copiado para a área de transferência");
				((ClipboardManager) getSystemService(getApplicationContext().CLIPBOARD_SERVICE)).setPrimaryClip(ClipData.newPlainText("clipboard", webview.getUrl()));
			}
		});
		
		linear_tradutor_google.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View _view) {
				String url = webview.getUrl();
				if (url != null) {
					webview.loadUrl("https://translate.google.com/translate?sl=en&tl=pt&hl=pt-BR&u=" + url.replace("https://", "") + "&client=webapp");
					SketchwareUtil.showMessage(getApplicationContext(), "Traduzindo");
				} else {
					SketchwareUtil.showMessage(getApplicationContext(), "Nenhuma URL encontrada para traduzir");
				}
				
			}
		});
		
		linear_modo_leitura.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View _view) {
				toque++;
				if ((toque % 2) == 1) {
					linear_modo_leitura.setAlpha((float)(0.5d));
					webview.setWebViewClient(new WebViewClient(){
						@Override
						public void onPageFinished(WebView v, String url){
							String js =
							"javascript:(function(){"
							
							// container principal
							+ "var wrap=document.createElement('div');"
							+ "wrap.style.maxWidth='700px';"
							+ "wrap.style.margin='20px auto';"
							+ "wrap.style.padding='20px';"
							+ "wrap.style.fontFamily='sans-serif';"
							+ "wrap.style.fontSize='21px';"
							+ "wrap.style.lineHeight='1.7';"
							+ "wrap.style.color='#ECF0F1';"
							+ "wrap.style.background='#2C3E50';"
							+ "wrap.style.borderRadius='10px';"
							
							// elementos permitidos
							+ "var els=document.querySelectorAll('p,h1,h2,h3,img');"
							+ "for(var i=0;i<els.length;i++){"
							+ "   var el=els[i].cloneNode(true);"
							
							// imagens
							+ "   if(el.tagName==='IMG'){"
							+ "       var w=els[i].naturalWidth || els[i].width;"
							+ "       var h=els[i].naturalHeight || els[i].height;"
							
							// remove imagens pequenas (ícones inúteis)
							+ "       if(w<=80 || h<=80){ continue; }"
							
							// estilizar imagens
							+ "       el.style.display='block';"
							+ "       el.style.margin='25px auto';"
							+ "       el.style.maxWidth='100%';"
							+ "       el.style.height='auto';"
							+ "       el.style.borderRadius='10px';"
							+ "       el.style.boxShadow='0 0 15px rgba(0,0,0,0.4)';"
							+ "   }"
							
							// adiciona ao container
							+ "   wrap.appendChild(el);"
							+ "}"
							
							// limpar a página
							+ "document.body.innerHTML='';"
							+ "document.body.style.background='#2C3E50';"
							+ "document.body.appendChild(wrap);"
							
							+ "})();";
							
							if(android.os.Build.VERSION.SDK_INT>=19){
								v.evaluateJavascript(js, null);
							} else {
								v.loadUrl(js);
							}
						}
					});
				} else {
					linear_modo_leitura.setAlpha((float)(1.0d));
					webview.setWebViewClient(new WebViewClient(){
						@Override
						public void onPageFinished(WebView v, String url){
							String js =
							"javascript:(function(){"
							// resetando estilo do body
							+ "document.body.style.background='';"
							+ "document.body.style.color='';"
							+ "document.body.style.margin='';"
							+ "document.body.style.maxWidth='';"
							+ "document.body.style.padding='';"
							// mostrando de volta elementos que o modo leitura escondeu
							+ "document.querySelectorAll('header, nav, footer, aside').forEach(e => e.style.display='');"
							+ "})();";
							
							if(android.os.Build.VERSION.SDK_INT >= 19){
								v.evaluateJavascript(js, null);
							} else {
								v.loadUrl(js);
							}
						}
					});
				}
				webview.loadUrl(webview.getUrl());
			}
		});
		
		linear_abrir_no_navegador.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View _view) {
				intent.setAction(Intent.ACTION_VIEW);
				intent.setData(Uri.parse(webview.getUrl()));
				startActivity(intent);
			}
		});
		
		checkbox_brilho.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton _param1, boolean _param2) {
				final boolean _isChecked = _param2;
				if (_isChecked) {
					Dados.edit().putString("brilho", "ativado").apply();
					webview.setAlpha((float)(0.5d));
				} else {
					Dados.edit().putString("brilho", "desativado").apply();
					webview.setAlpha((float)(1.0d));
				}
			}
		});
		
		checkbox_javascript2.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton _param1, boolean _param2) {
				final boolean _isChecked = _param2;
				if (_isChecked) {
					Dados.edit().putString("java", "ativado").apply();
					webview.getSettings().setJavaScriptEnabled(true); 
				} else {
					webview.getSettings().setJavaScriptEnabled(false); 
					Dados.edit().putString("java", "desativado").apply();
				}
			}
		});
		
		b1.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View _view) {
				tela1.setVisibility(View.GONE);
				tela2.setVisibility(View.VISIBLE);
				tela3.setVisibility(View.GONE);
			}
		});
		
		b2.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View _view) {
				tela1.setVisibility(View.GONE);
				tela2.setVisibility(View.GONE);
				tela3.setVisibility(View.VISIBLE);
			}
		});
	}
	

	private boolean submitSearch() {
		String input = edittext1 == null || edittext1.getText() == null
			? "" : edittext1.getText().toString().trim();
		if (input.isEmpty()) {
			openSearchScreen();
			return false;
		}

		String destination = resolveNavigationInput(input);
		if (destination == null) return false;

		webview.loadUrl(destination);
		tela1.setVisibility(View.GONE);
		tela2.setVisibility(View.VISIBLE);
		tela3.setVisibility(View.GONE);
		vazio.setVisibility(View.GONE);

		edittext1.setText("");
		edittext1.clearFocus();
		android.view.inputmethod.InputMethodManager keyboard =
			(android.view.inputmethod.InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
		if (keyboard != null) {
			keyboard.hideSoftInputFromWindow(edittext1.getWindowToken(), 0);
		}
		Dados.edit().putString("vazio", "desativado").apply();
		logDiagnostic("Pesquisa enviada pelo botão ou teclado");
		return true;
	}

	private void openSearchScreen() {
		if (tela1 != null) tela1.setVisibility(View.VISIBLE);
		if (tela2 != null) tela2.setVisibility(View.GONE);
		if (tela3 != null) tela3.setVisibility(View.GONE);
		if (edittext1 == null) return;

		edittext1.setShowSoftInputOnFocus(true);
		edittext1.requestFocus();
		getWindow().setSoftInputMode(
			android.view.WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE
				| android.view.WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE
		);
		edittext1.post(new Runnable() {
			@Override
			public void run() {
				if (isFinishing() || !edittext1.isShown()) return;
				android.view.inputmethod.InputMethodManager keyboard =
					(android.view.inputmethod.InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
				if (keyboard != null) {
					keyboard.showSoftInput(edittext1, android.view.inputmethod.InputMethodManager.SHOW_IMPLICIT);
				}
			}
		});
	}

	private static final String DIAGNOSTIC_FILE = "snav-diagnostic.log";
	private static final String PREVIOUS_DIAGNOSTIC_FILE = "snav-diagnostic.log.old";
	private static final String PENDING_CRASH_FILE = "snav-crash-pending";
	private static final int MAX_DIAGNOSTIC_BYTES = 262144;
	private static volatile java.io.File diagnosticDirectory;
	private static boolean diagnosticCrashHandlerInstalled;

	private static final java.util.concurrent.ThreadPoolExecutor DIAGNOSTIC_EXECUTOR =
		new java.util.concurrent.ThreadPoolExecutor(
			1, 1, 0L, java.util.concurrent.TimeUnit.MILLISECONDS,
			new java.util.concurrent.ArrayBlockingQueue<Runnable>(128),
			new java.util.concurrent.ThreadFactory() {
				@Override
				public Thread newThread(Runnable task) {
					Thread thread = new Thread(task, "Snav-Diagnostics");
					thread.setDaemon(true);
					return thread;
				}
			},
			new java.util.concurrent.ThreadPoolExecutor.DiscardOldestPolicy()
		);

	private String resolveNavigationInput(String input) {
		String value = input == null ? "" : input.trim();
		if (value.isEmpty()) return null;
		if (value.matches("(?i)^https?://.*")) {
			if (value.matches(".*\\s+.*")) return buildSearchUrl(value);
			try {
				Uri parsed = Uri.parse(value);
				if (parsed.getHost() != null) return value;
			} catch (Exception ignored) {
			}
			return buildSearchUrl(value);
		}

		if (!value.matches(".*\\s+.*") && isDomainOrIp(value)) {
			String candidate = "https://" + value;
			try {
				if (Uri.parse(candidate).getHost() != null) return candidate;
			} catch (Exception ignored) {
			}
		}
		return buildSearchUrl(value);
	}

	private boolean isDomainOrIp(String value) {
		if (value == null || value.isEmpty()) return false;
		return value.matches("(?i)^(?:[a-z0-9](?:[a-z0-9-]{0,61}[a-z0-9])?\\.)+[a-z]{2,}(?::\\d{1,5})?(?:[/?#][^\\s]*)?$")
			|| value.matches("^(?:\\d{1,3}\\.){3}\\d{1,3}(?::\\d{1,5})?(?:[/?#][^\\s]*)?$")
			|| value.matches("(?i)^localhost(?::\\d{1,5})?(?:/[^\\s]*)?$");
	}

	private String buildSearchUrl(String query) {
		try {
			return "https://www.google.com/search?q="
				+ java.net.URLEncoder.encode(query, "UTF-8");
		} catch (Exception error) {
			logDiagnostic("Falha ao codificar consulta de pesquisa");
			return null;
		}
	}

	private void startDiagnostics() {
		final java.io.File directory = getFilesDir();
		diagnosticDirectory = directory;
		synchronized (MainActivity.class) {
			if (!diagnosticCrashHandlerInstalled) {
				final Thread.UncaughtExceptionHandler previous =
					Thread.getDefaultUncaughtExceptionHandler();
				try {
					Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
						@Override
						public void uncaughtException(Thread thread, Throwable error) {
							writeDiagnosticSynchronously(directory,
								"CRASH NÃO TRATADO na thread " + thread.getName(), error);
							try {
								java.io.File marker = new java.io.File(directory, PENDING_CRASH_FILE);
								java.io.FileOutputStream output = new java.io.FileOutputStream(marker, false);
								output.write(String.valueOf(System.currentTimeMillis()).getBytes("UTF-8"));
								output.close();
							} catch (Throwable markerError) {
								android.util.Log.e("SnavDiagnostic", "Falha ao salvar marcador de crash", markerError);
							}
							if (previous != null) {
								previous.uncaughtException(thread, error);
							} else {
								android.os.Process.killProcess(android.os.Process.myPid());
								System.exit(10);
							}
						}
					});
					diagnosticCrashHandlerInstalled = true;
				} catch (Throwable handlerError) {
					android.util.Log.e("SnavDiagnostic", "Não foi possível instalar o capturador de falhas", handlerError);
				}
			}
		}
		logDiagnostic("========== INICIALIZAÇÃO DO SNAV ==========");
		logDiagnostic("Android=" + android.os.Build.VERSION.RELEASE
			+ " (SDK " + android.os.Build.VERSION.SDK_INT + ")");
		logDiagnostic("Dispositivo=" + android.os.Build.MANUFACTURER
			+ " " + android.os.Build.MODEL);
		logDiagnostic("Processo=" + android.os.Process.myPid());
	}

	private void logDiagnostic(final String event) {
		final java.io.File directory = diagnosticDirectory;
		if (directory == null || event == null) return;
		try {
			DIAGNOSTIC_EXECUTOR.execute(new Runnable() {
				@Override
				public void run() {
					writeDiagnosticSynchronously(directory, event, null);
				}
			});
		} catch (RuntimeException ignored) {
			android.util.Log.w("SnavDiagnostic", "Evento de diagnóstico descartado");
		}
	}

	private static synchronized void writeDiagnosticSynchronously(
		java.io.File directory, String event, Throwable error) {
		if (directory == null) return;
		java.io.FileOutputStream output = null;
		java.io.OutputStreamWriter writer = null;
		try {
			if (!directory.exists() && !directory.mkdirs()) return;
			java.io.File file = new java.io.File(directory, DIAGNOSTIC_FILE);
			if (file.exists() && file.length() >= MAX_DIAGNOSTIC_BYTES) {
				java.io.File previous = new java.io.File(directory, PREVIOUS_DIAGNOSTIC_FILE);
				if (previous.exists()) previous.delete();
				if (!file.renameTo(previous)) file.delete();
			}
			output = new java.io.FileOutputStream(file, true);
			writer = new java.io.OutputStreamWriter(output, "UTF-8");
			writer.write(new java.text.SimpleDateFormat(
				"yyyy-MM-dd HH:mm:ss.SSS", java.util.Locale.US).format(new java.util.Date()));
			writer.write(" [");
			writer.write(Thread.currentThread().getName());
			writer.write("] ");
			writer.write(event);
			writer.write("\n");
			if (error != null) {
				java.io.StringWriter trace = new java.io.StringWriter();
				error.printStackTrace(new java.io.PrintWriter(trace));
				String stack = trace.toString();
				if (stack.length() > 32768) stack = stack.substring(0, 32768);
				writer.write(stack);
				writer.write("\n");
			}
			writer.flush();
		} catch (Throwable logError) {
			android.util.Log.e("SnavDiagnostic", "Falha ao gravar diagnóstico", logError);
		} finally {
			try {
				if (writer != null) writer.close();
				else if (output != null) output.close();
			} catch (Exception ignored) {
			}
		}
	}

	private String safeHostForDiagnostics(String address) {
		try {
			String host = address == null ? null : Uri.parse(address).getHost();
			return host == null ? "(host indisponível)" : host;
		} catch (Exception ignored) {
			return "(host inválido)";
		}
	}

	private void addDiagnosticMenuAction() {
		if (linear33 == null) return;
		TextView action = new TextView(this);
		action.setText("Copiar diagnóstico");
		action.setTextColor(getResources().getColor(R.color.snav_on_surface));
		action.setTextSize(14);
		action.setGravity(Gravity.CENTER_VERTICAL);
		int horizontal = (int) (16 * getResources().getDisplayMetrics().density + 0.5f);
		int vertical = (int) (14 * getResources().getDisplayMetrics().density + 0.5f);
		action.setPadding(horizontal, vertical, horizontal, vertical);
		android.util.TypedValue rippleBackground = new android.util.TypedValue();
		if (getTheme().resolveAttribute(android.R.attr.selectableItemBackground, rippleBackground, true)) {
			action.setBackgroundResource(rippleBackground.resourceId);
		}
		action.setClickable(true);
		action.setFocusable(true);
		action.setContentDescription("Copiar relatório de diagnóstico do Snav");
		action.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				copyDiagnosticReport();
			}
		});
		linear33.addView(action, new LinearLayout.LayoutParams(
			LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
	}

	private void showPendingCrashReport() {
		final java.io.File marker = new java.io.File(getFilesDir(), PENDING_CRASH_FILE);
		if (!marker.exists()) return;
		new AlertDialog.Builder(this)
			.setTitle("Falha detectada no Snav")
			.setMessage("O Snav detectou que a execução anterior terminou inesperadamente. Você pode copiar o diagnóstico para ajudar a identificar a causa.")
			.setPositiveButton("Copiar diagnóstico", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					if (copyDiagnosticReport()) marker.delete();
				}
			})
			.setNegativeButton("Depois", null)
			.show();
	}

	private boolean copyDiagnosticReport() {
		try {
			StringBuilder report = new StringBuilder();
			report.append("SNAV BROWSER - RELATÓRIO DE DIAGNÓSTICO\n");
			report.append("Android: ").append(android.os.Build.VERSION.RELEASE)
				.append(" (SDK ").append(android.os.Build.VERSION.SDK_INT).append(")\n");
			report.append("Dispositivo: ").append(android.os.Build.MANUFACTURER)
				.append(" ").append(android.os.Build.MODEL).append("\n\n");
			appendDiagnosticFile(report, new java.io.File(getFilesDir(), PREVIOUS_DIAGNOSTIC_FILE));
			appendDiagnosticFile(report, new java.io.File(getFilesDir(), DIAGNOSTIC_FILE));
			ClipboardManager clipboard =
				(ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
			if (clipboard == null) return false;
			clipboard.setPrimaryClip(ClipData.newPlainText("Snav diagnóstico", report.toString()));
			Toast.makeText(this, "Diagnóstico copiado. Revise o conteúdo antes de compartilhar.",
				Toast.LENGTH_LONG).show();
			return true;
		} catch (Exception error) {
			android.util.Log.e("SnavDiagnostic", "Falha ao copiar diagnóstico", error);
			Toast.makeText(this, "Não foi possível copiar o diagnóstico.", Toast.LENGTH_LONG).show();
			return false;
		}
	}

	private void appendDiagnosticFile(StringBuilder report, java.io.File file) throws IOException {
		if (!file.exists()) return;
		report.append("--- ").append(file.getName()).append(" ---\n");
		java.io.BufferedReader reader = new java.io.BufferedReader(
			new java.io.InputStreamReader(new java.io.FileInputStream(file), "UTF-8"));
		try {
			String line;
			int total = 0;
			while ((line = reader.readLine()) != null && total < MAX_DIAGNOSTIC_BYTES * 2) {
				report.append(line).append('\n');
				total += line.length();
			}
		} finally {
			reader.close();
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		logDiagnostic("Ciclo de vida: onResume");
	}

	@Override
	protected void onPause() {
		logDiagnostic("Ciclo de vida: onPause");
		super.onPause();
	}

	@Override
	protected void onDestroy() {
		logDiagnostic("Ciclo de vida: onDestroy; isFinishing=" + isFinishing());
		super.onDestroy();
	}

	private final Handler progressHandler = new Handler(Looper.getMainLooper());
	private ObjectAnimator progressAnimator;
	private Runnable showProgressRunnable;
	private boolean pageLoading = false;

	private void beginPageProgress() {
		pageLoading = true;
		if (showProgressRunnable != null) {
			progressHandler.removeCallbacks(showProgressRunnable);
		}
		if (progressAnimator != null) {
			progressAnimator.cancel();
		}
		prog.animate().cancel();
		prog.setAlpha(1f);
		prog.setProgress(0);
		prog.setIndeterminate(true);

		showProgressRunnable = new Runnable() {
			@Override
			public void run() {
				if (pageLoading) {
					prog.setVisibility(View.VISIBLE);
				}
			}
		};
		progressHandler.postDelayed(showProgressRunnable, 120);
	}

	private void finishPageProgress() {
		if (!pageLoading) {
			return;
		}
		pageLoading = false;
		if (showProgressRunnable != null) {
			progressHandler.removeCallbacks(showProgressRunnable);
		}
		if (progressAnimator != null) {
			progressAnimator.cancel();
		}
		prog.setIndeterminate(false);
		prog.setProgress(100);

		if (prog.getVisibility() != View.VISIBLE) {
			prog.setProgress(0);
			return;
		}

		prog.animate()
			.alpha(0f)
			.setDuration(160)
			.withEndAction(new Runnable() {
				@Override
				public void run() {
					prog.setVisibility(View.GONE);
					prog.setAlpha(1f);
					prog.setProgress(0);
				}
			})
			.start();
	}

	private int siteInfoDp(float value) {
		return (int) (value * getResources().getDisplayMetrics().density + 0.5f);
	}

	private void addSiteInfoRow(LinearLayout parent, String title, String value) {
		TextView heading = new TextView(this);
		heading.setText(title);
		heading.setTextColor(Color.parseColor("#DCE1E6"));
		heading.setTextSize(12);
		parent.addView(heading);

		TextView detail = new TextView(this);
		detail.setText(value);
		detail.setTextColor(Color.WHITE);
		detail.setTextSize(14);
		detail.setTextIsSelectable(true);
		detail.setPadding(0, siteInfoDp(3), 0, siteInfoDp(12));
		parent.addView(detail);
	}

	private void showSiteInfoBottomSheet() {
		final String currentUrl = webview.getUrl();
		final Uri pageUri = currentUrl == null ? null : Uri.parse(currentUrl);
		final String scheme = pageUri == null ? null : pageUri.getScheme();
		final String host = pageUri == null ? null : pageUri.getHost();
		final String origin;
		if (scheme != null && host != null) {
			int port = pageUri.getPort();
			origin = scheme + "://" + host + (port >= 0 ? ":" + port : "");
		} else {
			origin = null;
		}

		final Dialog sheet = new Dialog(this);
		LinearLayout content = new LinearLayout(this);
		content.setOrientation(LinearLayout.VERTICAL);
		content.setPadding(siteInfoDp(24), siteInfoDp(22), siteInfoDp(24), siteInfoDp(28));
		GradientDrawable surface = new GradientDrawable();
		surface.setColor(Color.parseColor("#34495E"));
		surface.setCornerRadii(new float[] { siteInfoDp(24), siteInfoDp(24), siteInfoDp(24), siteInfoDp(24), 0, 0, 0, 0 });
		content.setBackground(surface);

		TextView title = new TextView(this);
		title.setText("Informações do site");
		title.setTextColor(Color.WHITE);
		title.setTextSize(20);
		title.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
		title.setPadding(0, 0, 0, siteInfoDp(18));
		content.addView(title);

		addSiteInfoRow(content, "ENDEREÇO COMPLETO", currentUrl == null ? "Nenhum endereço aberto" : currentUrl);
		String connection;
		if ("https".equalsIgnoreCase(scheme)) {
			connection = "HTTPS — conexão criptografada";
		} else if ("http".equalsIgnoreCase(scheme)) {
			connection = "HTTP — conexão não criptografada";
		} else {
			connection = "Não disponível";
		}
		addSiteInfoRow(content, "CONEXÃO", connection);
		addSiteInfoRow(content, "PERMISSÕES DO SITE", "O Snav não concede permissões WebView às páginas. Solicitações de recursos como câmera e microfone permanecem bloqueadas.");
		addSiteInfoRow(content, "COOKIES E DADOS", "Os cookies são gerenciados pelo WebView. A remoção individual de cookies por domínio ainda não está disponível. O botão abaixo remove o armazenamento web deste domínio.");

		TextView clearData = new TextView(this);
		clearData.setText("Limpar armazenamento local");
		clearData.setTextColor(Color.WHITE);
		clearData.setTextSize(14);
		clearData.setGravity(Gravity.CENTER);
		clearData.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
		clearData.setMinHeight(siteInfoDp(48));
		clearData.setPadding(siteInfoDp(16), siteInfoDp(10), siteInfoDp(16), siteInfoDp(10));
		GradientDrawable actionBg = new GradientDrawable();
		actionBg.setColor(Color.parseColor("#E67E22"));
		actionBg.setCornerRadius(siteInfoDp(24));
		clearData.setBackgroundResource(R.drawable.snav_accent_ripple);
		clearData.setClickable(true);
		clearData.setFocusable(true);
		content.addView(clearData, new LinearLayout.LayoutParams(-1, -2));
		clearData.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				if (origin == null) {
					Toast.makeText(MainActivity.this, "Nenhum domínio disponível", Toast.LENGTH_SHORT).show();
					return;
				}
				new AlertDialog.Builder(MainActivity.this)
					.setTitle("Limpar dados do site")
					.setMessage("Remover o armazenamento local de " + host + "? Os cookies não serão removidos.")
					.setNegativeButton("Cancelar", null)
					.setPositiveButton("Limpar", new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							WebStorage.getInstance().deleteOrigin(origin);
							Toast.makeText(MainActivity.this, "Armazenamento local removido", Toast.LENGTH_SHORT).show();
						}
					}).show();
			}
		});

		addSiteInfoRow(content, "CONFIGURAÇÕES DO SITE", "JavaScript: " + (webview.getSettings().getJavaScriptEnabled() ? "ativado" : "desativado") + ". Esta configuração é global para o navegador, não específica deste domínio.");

		ScrollView scroll = new ScrollView(this);
		scroll.setFillViewport(true);
		scroll.setClipToPadding(false);
		scroll.addView(content, new ScrollView.LayoutParams(-1, -2));
		sheet.setContentView(scroll);
		sheet.setCancelable(true);
		Window window = sheet.getWindow();
		if (window != null) {
			window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
			window.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
			WindowManager.LayoutParams params = window.getAttributes();
			params.width = WindowManager.LayoutParams.MATCH_PARENT;
			params.height = WindowManager.LayoutParams.WRAP_CONTENT;
			params.gravity = Gravity.BOTTOM;
			params.dimAmount = 0.38f;
			window.setAttributes(params);
		}
		sheet.show();
		if (sheet.getWindow() != null) {
			sheet.getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
		}
	}



	private void grantWebPermissionWhenAndroidAllows(PermissionRequest request, String[] resources) {
		if (request == null || isFinishing()) return;
		java.util.ArrayList<String> required = new java.util.ArrayList<String>();
		for (String resource : resources) {
			if (PermissionRequest.RESOURCE_VIDEO_CAPTURE.equals(resource)) {
				required.add(android.Manifest.permission.CAMERA);
			} else if (PermissionRequest.RESOURCE_AUDIO_CAPTURE.equals(resource)) {
				required.add(android.Manifest.permission.RECORD_AUDIO);
			} else {
				request.deny();
				return;
			}
		}
		if (Build.VERSION.SDK_INT >= 23) {
			java.util.ArrayList<String> missing = new java.util.ArrayList<String>();
			for (String permission : required) {
				if (checkSelfPermission(permission) != android.content.pm.PackageManager.PERMISSION_GRANTED) missing.add(permission);
			}
			if (!missing.isEmpty()) {
				pendingWebPermissionRequest = request;
				requestPermissions(missing.toArray(new String[missing.size()]), 4101);
				return;
			}
		}
		request.grant(resources);
	}

	private void grantGeolocationWhenAndroidAllows(String origin, GeolocationPermissions.Callback callback) {
		if (Build.VERSION.SDK_INT >= 23
			&& checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION) != android.content.pm.PackageManager.PERMISSION_GRANTED
			&& checkSelfPermission(android.Manifest.permission.ACCESS_COARSE_LOCATION) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
			pendingGeolocationOrigin = origin;
			pendingGeolocationCallback = callback;
			requestPermissions(new String[] {
				android.Manifest.permission.ACCESS_FINE_LOCATION,
				android.Manifest.permission.ACCESS_COARSE_LOCATION
			}, 4102);
			return;
		}
		callback.invoke(origin, true, false);
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);
		if (requestCode == 4101) {
			PermissionRequest pending = pendingWebPermissionRequest;
			pendingWebPermissionRequest = null;
			if (pending == null) return;
			boolean granted = grantResults.length > 0;
			for (int result : grantResults) granted = granted && result == android.content.pm.PackageManager.PERMISSION_GRANTED;
			if (granted) pending.grant(pending.getResources()); else pending.deny();
		} else if (requestCode == 4102) {
			GeolocationPermissions.Callback callback = pendingGeolocationCallback;
			String origin = pendingGeolocationOrigin;
			pendingGeolocationCallback = null;
			pendingGeolocationOrigin = null;
			if (callback == null) return;
			boolean granted = Build.VERSION.SDK_INT < 23
				|| checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION) == android.content.pm.PackageManager.PERMISSION_GRANTED
				|| checkSelfPermission(android.Manifest.permission.ACCESS_COARSE_LOCATION) == android.content.pm.PackageManager.PERMISSION_GRANTED;
			callback.invoke(origin, granted, false);
		}
	}

	private void addThirdPartyCookiePreference(boolean enabled) {
		if (linear33 == null || android.os.Build.VERSION.SDK_INT < 21
			|| linear33.findViewWithTag("snav_third_party_cookie_preference") != null) return;
		LinearLayout row = new LinearLayout(this);
		row.setTag("snav_third_party_cookie_preference");
		row.setOrientation(LinearLayout.VERTICAL);
		row.setPadding(dp(8), dp(8), dp(8), dp(8));
		MaterialSwitch toggle = new MaterialSwitch(this);
		toggle.setText("Cookies de terceiros");
		toggle.setTextColor(getResources().getColor(R.color.snav_on_surface));
		toggle.setTextSize(14);
		toggle.setChecked(enabled);
		TextView summary = new TextView(this);
		summary.setText("Bloqueados por padrão; permitir pode aumentar o rastreamento entre sites.");
		summary.setTextColor(getResources().getColor(R.color.snav_on_surface_variant));
		summary.setTextSize(12);
		summary.setPadding(dp(40), 0, dp(8), 0);
		row.addView(toggle, new LinearLayout.LayoutParams(-1, dp(48)));
		row.addView(summary, new LinearLayout.LayoutParams(-1, -2));
		toggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override public void onCheckedChanged(CompoundButton button, boolean checked) {
				Dados.edit().putBoolean("third_party_cookies", checked).apply();
				CookieManager.getInstance().setAcceptThirdPartyCookies(webview, checked);
				android.widget.Toast.makeText(MainActivity.this,
					checked ? "Cookies de terceiros permitidos" : "Cookies de terceiros bloqueados",
					android.widget.Toast.LENGTH_SHORT).show();
			}
		});
		int insertAt = Math.max(0, linear33.getChildCount() - 1);
		linear33.addView(row, insertAt, new LinearLayout.LayoutParams(-1, -2));
	}

	private int dp(float value) {
		return (int) (value * getResources().getDisplayMetrics().density + 0.5f);
	}

	private void showInsecureConnectionDialog(final WebView view, final String url) {
		final Uri parsed = Uri.parse(url);
		final String host = parsed.getHost() == null ? "este endereço" : parsed.getHost();
		new AlertDialog.Builder(this)
			.setTitle("Conexão não segura")
			.setMessage("A conexão com " + host + " usa HTTP e não é criptografada. Seus dados podem ser interceptados. Deseja continuar mesmo assim?")
			.setPositiveButton("Continuar", new DialogInterface.OnClickListener() {
				@Override public void onClick(DialogInterface dialog, int which) {
					allowInsecureNavigationOnce = true;
					view.loadUrl(url);
				}
			})
			.setNegativeButton("Voltar", null)
			.setOnCancelListener(new DialogInterface.OnCancelListener() {
				@Override public void onCancel(DialogInterface dialog) { allowInsecureNavigationOnce = false; }
			}).show();
	}

	private void initializeLogic() {
		_armazenamento();
		_open_sans();
		_interfaces();
		_download();
		_internet();
		_gone();
	}
	
	@Override
	public void onBackPressed() {
		if (webview.canGoBack()) {
			webview.goBack();
		} else {
			tela1.setVisibility(View.VISIBLE);
			tela2.setVisibility(View.GONE);
			tela3.setVisibility(View.GONE);
			toque++;
			if (toque == 1) {
				time = new TimerTask() {
					@Override
					public void run() {
						runOnUiThread(new Runnable() {
							@Override
							public void run() {
								toque = 0;
							}
						});
					}
				};
				_timer.schedule(time, (int)(2000));
			} else {
				finish();
			}
		}
	}
	public void _gone() {
		//visibilidade
		//telas
		tela2.setVisibility(View.GONE);
		tela3.setVisibility(View.GONE);
	}
	
	
	public void _open_sans() {
		//fonte
		text_barra.setTypeface(Typeface.createFromAsset(getAssets(),"fonts/open_regular.ttf"), 0);
		edittext1.setTypeface(Typeface.createFromAsset(getAssets(),"fonts/open_regular.ttf"), 0);
		text_icon.setTypeface(Typeface.createFromAsset(getAssets(),"fonts/open_sans_bold.ttf"), 0);
		//text
		text1.setTypeface(Typeface.createFromAsset(getAssets(),"fonts/open_regular.ttf"), 0);
		text2.setTypeface(Typeface.createFromAsset(getAssets(),"fonts/open_regular.ttf"), 0);
		text5.setTypeface(Typeface.createFromAsset(getAssets(),"fonts/open_regular.ttf"), 0);
		text7.setTypeface(Typeface.createFromAsset(getAssets(),"fonts/open_regular.ttf"), 0);
		text14.setTypeface(Typeface.createFromAsset(getAssets(),"fonts/open_regular.ttf"), 0);
		text15.setTypeface(Typeface.createFromAsset(getAssets(),"fonts/open_regular.ttf"), 0);
		text19.setTypeface(Typeface.createFromAsset(getAssets(),"fonts/open_regular.ttf"), 0);
		//checkbox
		checkbox_javascript2.setTypeface(Typeface.createFromAsset(getAssets(),"fonts/open_regular.ttf"), 0);
		checkbox_brilho.setTypeface(Typeface.createFromAsset(getAssets(),"fonts/open_regular.ttf"), 0);
		//linear_c
		b1.setTypeface(Typeface.createFromAsset(getAssets(),"fonts/open_regular.ttf"), 0);
		b2.setTypeface(Typeface.createFromAsset(getAssets(),"fonts/open_regular.ttf"), 0);
	}
	
	
	public void _interfaces() {
		// A barra mantém o RippleDrawable definido no XML.
		linear_barra_pesquisa.setElevation(0);
		
		// vscroll1
		vscroll1.setVerticalScrollBarEnabled(false); vscroll1.setHorizontalScrollBarEnabled(false);
		// hscroll1
		hscroll1.setVerticalScrollBarEnabled(false); hscroll1.setHorizontalScrollBarEnabled(false);
	}
	
	
	public void _internet() {
		//internet
		// Barra de progresso MD3
		prog.setVisibility(View.GONE);
		prog.setAlpha(1f);
		prog.setProgress(0);
		prog.setIndeterminate(true);
		
		// Cookies
		android.webkit.CookieManager cookieManager = android.webkit.CookieManager.getInstance();
		cookieManager.setAcceptCookie(true);
		
		final boolean thirdPartyCookiesEnabled = Dados.getBoolean("third_party_cookies", false);
		if (android.os.Build.VERSION.SDK_INT >= 21) {
			cookieManager.setAcceptThirdPartyCookies(webview, thirdPartyCookiesEnabled);
		}
		addThirdPartyCookiePreference(thirdPartyCookiesEnabled);

		// Layout WebView
		webview.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);
		webview.setVerticalScrollBarEnabled(true);
		webview.setHorizontalScrollBarEnabled(false);
		webview.setLayoutParams(new LinearLayout.LayoutParams(
		ViewGroup.LayoutParams.MATCH_PARENT,
		ViewGroup.LayoutParams.MATCH_PARENT
		));
		
		// Settings
		WebSettings s = webview.getSettings();
		
		// Agente de usuário
		s.setUserAgentString(
		"Mozilla/5.0 (Linux; Android 16; SM-A065M) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Mobile Safari/537.36"
		);
		
		s.setJavaScriptEnabled(true);
		s.setDomStorageEnabled(true);
		s.setCacheMode(WebSettings.LOAD_DEFAULT);
		s.setSaveFormData(true);
		
		s.setSupportMultipleWindows(true);
		s.setJavaScriptCanOpenWindowsAutomatically(false);
		
		s.setAllowFileAccess(false);
		s.setAllowContentAccess(false);
		if (android.os.Build.VERSION.SDK_INT >= 16) {
			s.setAllowFileAccessFromFileURLs(false);
			s.setAllowUniversalAccessFromFileURLs(false);
		}
		
		try {
			s.setSafeBrowsingEnabled(true);
		} catch (Exception e) {}
		
		if (android.os.Build.VERSION.SDK_INT >= 21) {
			s.setMixedContentMode(
			WebSettings.MIXED_CONTENT_NEVER_ALLOW
			);
		}
		
		s.setLoadsImagesAutomatically(true);
		s.setBlockNetworkImage(false);
		s.setBlockNetworkLoads(false);
		s.setMediaPlaybackRequiresUserGesture(false);
		
		s.setUseWideViewPort(true);
		s.setLoadWithOverviewMode(true);
		
		if (android.os.Build.VERSION.SDK_INT >= 23) {
			try {
				s.setOffscreenPreRaster(true);
			} catch (Exception e) {}
		}
		
		// WebViewClient
		webview.setWebViewClient(new android.webkit.WebViewClient() {
			@Override
			public boolean shouldOverrideUrlLoading(final WebView view, final WebResourceRequest request) {
				if (android.os.Build.VERSION.SDK_INT >= 21 && request != null && !request.isForMainFrame()) {
					return false;
				}
				String target = request == null ? null : request.getUrl().toString();
				if (target == null || !target.regionMatches(true, 0, "http://", 0, 7)) return false;
				if (allowInsecureNavigationOnce) {
					allowInsecureNavigationOnce = false;
					return false;
				}
				showInsecureConnectionDialog(view, target);
				return true;
			}

			@Override
			public boolean shouldOverrideUrlLoading(WebView view, String url) {
				if (url == null || !url.regionMatches(true, 0, "http://", 0, 7)) return false;
				if (allowInsecureNavigationOnce) {
					allowInsecureNavigationOnce = false;
					return false;
				}
				showInsecureConnectionDialog(view, url);
				return true;
			}

			@Override
			public void onReceivedSslError(WebView view, SslErrorHandler handler, android.net.http.SslError error) {
				logDiagnostic("Erro TLS cancelado pelo navegador");
				handler.cancel();
			}

			@Override
			public void onPageStarted(WebView view, String url, Bitmap favicon) {
				super.onPageStarted(view, url, favicon);
				beginPageProgress();
			}

			@Override
			public void onPageFinished(WebView view, String url) {
				super.onPageFinished(view, url);
				finishPageProgress();
			}
		});
		
		// WebChromeClient
		webview.setWebChromeClient(new android.webkit.WebChromeClient() {
			
			private View customView;
			private CustomViewCallback customViewCallback;
			private FrameLayout fullscreenContainer;
			
			@Override
			public void onReceivedTitle(WebView v, String t) {
				text_barra.setText(t);
			}
			
			@Override
			public void onPermissionRequest(final PermissionRequest request) {
				if (request == null) return;
				final Uri origin = request.getOrigin();
				final String[] requested = request.getResources();
				if (origin == null || !"https".equalsIgnoreCase(origin.getScheme())
					|| requested == null || requested.length == 0) {
					request.deny();
					return;
				}
				final String host = origin.getHost() == null ? "site desconhecido" : origin.getHost();
				new AlertDialog.Builder(MainActivity.this)
					.setTitle("Permissão do site")
					.setMessage(host + " solicita acesso à câmera ou ao microfone. Permita somente se você iniciou essa ação e confia no site.")
					.setPositiveButton("Permitir", new DialogInterface.OnClickListener() {
						@Override public void onClick(DialogInterface dialog, int which) {
							grantWebPermissionWhenAndroidAllows(request, requested);
						}
					})
					.setNegativeButton("Bloquear", new DialogInterface.OnClickListener() {
						@Override public void onClick(DialogInterface dialog, int which) { request.deny(); }
					})
					.setOnCancelListener(new DialogInterface.OnCancelListener() {
						@Override public void onCancel(DialogInterface dialog) { request.deny(); }
					}).show();
			}

			@Override
			public void onGeolocationPermissionsShowPrompt(final String origin, final GeolocationPermissions.Callback callback) {
				Uri parsed = Uri.parse(origin == null ? "" : origin);
				if (!"https".equalsIgnoreCase(parsed.getScheme())) {
					callback.invoke(origin, false, false);
					return;
				}
				new AlertDialog.Builder(MainActivity.this)
					.setTitle("Permitir localização?")
					.setMessage("O site " + (parsed.getHost() == null ? "" : parsed.getHost()) + " solicita sua localização. A permissão vale somente para esta solicitação.")
					.setPositiveButton("Permitir", new DialogInterface.OnClickListener() {
						@Override public void onClick(DialogInterface dialog, int which) {
							grantGeolocationWhenAndroidAllows(origin, callback);
						}
					})
					.setNegativeButton("Bloquear", new DialogInterface.OnClickListener() {
						@Override public void onClick(DialogInterface dialog, int which) { callback.invoke(origin, false, false); }
					})
					.setOnCancelListener(new DialogInterface.OnCancelListener() {
						@Override public void onCancel(DialogInterface dialog) { callback.invoke(origin, false, false); }
					}).show();
			}

			@Override
			public void onProgressChanged(WebView v, int p) {
				if (!pageLoading) {
					beginPageProgress();
				}
				if (p > 0) {
					prog.setIndeterminate(false);
					if (progressAnimator != null) {
						progressAnimator.cancel();
					}
					progressAnimator = ObjectAnimator.ofInt(prog, "progress", prog.getProgress(), Math.min(100, p));
					progressAnimator.setDuration(100);
					progressAnimator.start();
				}
				if (p >= 100) {
					finishPageProgress();
				}
			}
			
			@Override
			public void onShowCustomView(View v, CustomViewCallback cb) {
				
				if (customView != null) {
					cb.onCustomViewHidden();
					return;
				}
				
				customView = v;
				customViewCallback = cb;
				
				FrameLayout decor =
				(FrameLayout) getWindow().getDecorView();
				
				fullscreenContainer =
				new FrameLayout(MainActivity.this);
				
				fullscreenContainer.setBackgroundColor(Color.BLACK);
				
				fullscreenContainer.addView(
				v,
				new FrameLayout.LayoutParams(-1, -1)
				);
				
				decor.addView(fullscreenContainer);
				
				// Fullscreen real
				decor.setSystemUiVisibility(
				View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
				| View.SYSTEM_UI_FLAG_FULLSCREEN
				| View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
				| View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
				| View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
				);
			}
			
			@Override
			public void onHideCustomView() {
				
				FrameLayout decor =
				(FrameLayout) getWindow().getDecorView();
				
				if (fullscreenContainer != null) {
					decor.removeView(fullscreenContainer);
				}
				
				fullscreenContainer = null;
				customView = null;
				
				// Volta UI normal
				decor.setSystemUiVisibility(
				View.SYSTEM_UI_FLAG_VISIBLE
				);
				
				if (customViewCallback != null) {
					customViewCallback.onCustomViewHidden();
				}
			}
		});
		
		// Pesquisa e navegação pelo teclado.
		edittext1.setOnEditorActionListener(new TextView.OnEditorActionListener() {
			@Override
			public boolean onEditorAction(TextView view, int actionId, KeyEvent event) {
				boolean enterPressed = event != null
					&& event.getKeyCode() == KeyEvent.KEYCODE_ENTER
					&& event.getAction() == KeyEvent.ACTION_DOWN;
				boolean imeAction = actionId == android.view.inputmethod.EditorInfo.IME_ACTION_SEARCH
					|| actionId == android.view.inputmethod.EditorInfo.IME_ACTION_GO
					|| actionId == android.view.inputmethod.EditorInfo.IME_ACTION_DONE;
				if (!imeAction && !enterPressed) return false;
				submitSearch();
				return true;
			}
		});
		//wallpaper
		// ============================================================
		// WALLPAPER DE PAISAGENS - WIKIMEDIA COMMONS
		// Sketchware Pro 7.0.0
		// Java 7
		//
		// LinearLayout: tela1
		//
		// - Paisagens horizontais
		// - Montanhas, florestas, praias, lagos, natureza, pôr do sol
		// - Wallpaper fica atrás dos componentes
		// - Cache local
		// - Atualização automática
		// - Economia de dados móveis
		// - Escurecimento do wallpaper
		// - Sem biblioteca externa
		// ============================================================
		
		final android.view.ViewGroup wallpaperLayout = tela1;
		
		final java.io.File wallpaperFile =
		new java.io.File(
		getCacheDir(),
		"wikimedia_landscape.jpg"
		);
		
		final java.io.File wallpaperTimeFile =
		new java.io.File(
		getCacheDir(),
		"wikimedia_landscape_time.txt"
		);
		
		
		// ============================================================
		// CONFIGURAÇÕES
		// ============================================================
		
		// Wi-Fi: atualiza a cada 6 horas
		final long WIFI_CACHE_TIME =
		6L * 60L * 60L * 1000L;
		
		// Dados móveis: atualiza a cada 24 horas
		final long MOBILE_CACHE_TIME =
		24L * 60L * 60L * 1000L;
		
		
		// Escurecimento do wallpaper
		//
		// 0   = sem escurecimento
		// 60  = leve
		// 90  = recomendado
		// 120 = escuro
		// 160 = muito escuro
		
		final int DARK_ALPHA = 90;
		
		
		// ============================================================
		// DRAWABLE DO WALLPAPER
		// ============================================================
		
		class LandscapeDrawable
		extends android.graphics.drawable.Drawable {
			
			private android.graphics.Bitmap bitmap;
			
			private android.graphics.Paint bitmapPaint;
			
			private android.graphics.Paint darkPaint;
			
			
			public LandscapeDrawable(
			android.graphics.Bitmap b) {
				
				bitmap = b;
				
				
				bitmapPaint =
				new android.graphics.Paint(
				android.graphics.Paint.ANTI_ALIAS_FLAG
				|
				android.graphics.Paint.FILTER_BITMAP_FLAG
				);
				
				
				darkPaint =
				new android.graphics.Paint(
				android.graphics.Paint.ANTI_ALIAS_FLAG
				);
				
				
				darkPaint.setColor(
				android.graphics.Color.argb(
				DARK_ALPHA,
				0,
				0,
				0
				)
				);
			}
			
			
			@Override
			public void draw(
			android.graphics.Canvas canvas) {
				
				if (bitmap == null) {
					return;
				}
				
				
				int width =
				getBounds().width();
				
				int height =
				getBounds().height();
				
				
				if (width <= 0 || height <= 0) {
					return;
				}
				
				
				float bitmapWidth =
				bitmap.getWidth();
				
				float bitmapHeight =
				bitmap.getHeight();
				
				
				// ========================================================
				// CENTER CROP
				// ========================================================
				
				float scale =
				Math.max(
				width / bitmapWidth,
				height / bitmapHeight
				);
				
				
				float newWidth =
				bitmapWidth * scale;
				
				
				float newHeight =
				bitmapHeight * scale;
				
				
				float left =
				(width - newWidth) / 2f;
				
				
				float top =
				(height - newHeight) / 2f;
				
				
				android.graphics.RectF destination =
				new android.graphics.RectF(
				left,
				top,
				left + newWidth,
				top + newHeight
				);
				
				
				// Desenha a paisagem
				canvas.drawBitmap(
				bitmap,
				null,
				destination,
				bitmapPaint
				);
				
				
				// ========================================================
				// CAMADA ESCURA
				// ========================================================
				
				canvas.drawRect(
				0,
				0,
				width,
				height,
				darkPaint
				);
			}
			
			
			@Override
			public void setAlpha(
			int alpha) {
				
				bitmapPaint.setAlpha(alpha);
			}
			
			
			@Override
			public void setColorFilter(
			android.graphics.ColorFilter colorFilter) {
				
				bitmapPaint.setColorFilter(
				colorFilter
				);
			}
			
			
			@Override
			public int getOpacity() {
				
				return android.graphics.PixelFormat.OPAQUE;
			}
		}
		
		
		// ============================================================
		// BITMAP ATUAL
		// ============================================================
		
		final android.graphics.Bitmap[] currentBitmap =
		new android.graphics.Bitmap[1];
		
		
		// ============================================================
		// ATUALIZA O BACKGROUND QUANDO A TELA FOR MEDIDA
		// ============================================================
		
		final android.view.ViewTreeObserver.OnGlobalLayoutListener
		landscapeLayoutListener =
		new android.view.ViewTreeObserver.OnGlobalLayoutListener() {
			
			@Override
			public void onGlobalLayout() {
				
				if (currentBitmap[0] == null) {
					return;
				}
				
				
				LandscapeDrawable drawable =
				new LandscapeDrawable(
				currentBitmap[0]
				);
				
				
				wallpaperLayout.setBackgroundDrawable(
				drawable
				);
			}
		};
		
		
		wallpaperLayout
		.getViewTreeObserver()
		.addOnGlobalLayoutListener(
		landscapeLayoutListener
		);
		
		
		// ============================================================
		// CARREGA A PAISAGEM DO CACHE
		// ============================================================
		
		if (wallpaperFile.exists()
		&& wallpaperFile.length() > 0) {
			
			android.graphics.Bitmap cachedBitmap =
			android.graphics.BitmapFactory.decodeFile(
			wallpaperFile.getAbsolutePath()
			);
			
			
			if (cachedBitmap != null) {
				
				currentBitmap[0] =
				cachedBitmap;
				
				
				LandscapeDrawable drawable =
				new LandscapeDrawable(
				cachedBitmap
				);
				
				
				wallpaperLayout.setBackgroundDrawable(
				drawable
				);
			}
		}
		
		
		// ============================================================
		// VERIFICA CONEXÃO
		// ============================================================
		
		boolean internetAvailable = false;
		
		boolean mobileNetwork = false;
		
		
		try {
			
			android.net.ConnectivityManager
			connectivityManager =
			(android.net.ConnectivityManager)
			getSystemService(
			android.content.Context.CONNECTIVITY_SERVICE
			);
			
			
			android.net.NetworkInfo networkInfo =
			connectivityManager.getActiveNetworkInfo();
			
			
			if (networkInfo != null
			&& networkInfo.isConnected()) {
				
				internetAvailable = true;
				
				
				if (android.os.Build.VERSION.SDK_INT >= 16) {
					
					mobileNetwork =
					connectivityManager
					.isActiveNetworkMetered();
				}
			}
			
		} catch (Exception e) {
			
			internetAvailable = false;
		}
		
		
		// ============================================================
		// LÊ HORÁRIO DO CACHE
		// ============================================================
		
		long savedTime = 0;
		
		
		try {
			
			if (wallpaperTimeFile.exists()) {
				
				java.io.BufferedReader reader =
				new java.io.BufferedReader(
				new java.io.FileReader(
				wallpaperTimeFile
				)
				);
				
				
				String time =
				reader.readLine();
				
				
				reader.close();
				
				
				if (time != null
				&& time.trim().length() > 0) {
					
					savedTime =
					Long.parseLong(
					time.trim()
					);
				}
			}
			
		} catch (Exception e) {
			
			savedTime = 0;
		}
		
		
		// ============================================================
		// IDADE DO CACHE
		// ============================================================
		
		long cacheAge =
		System.currentTimeMillis()
		- savedTime;
		
		
		// ============================================================
		// TEMPO MÁXIMO DO CACHE
		// ============================================================
		
		long maxCacheTime;
		
		
		if (mobileNetwork) {
			
			maxCacheTime =
			MOBILE_CACHE_TIME;
			
		} else {
			
			maxCacheTime =
			WIFI_CACHE_TIME;
		}
		
		
		// ============================================================
		// VERIFICA SE PRECISA DE UMA NOVA PAISAGEM
		// ============================================================
		
		boolean needNewWallpaper =
		!wallpaperFile.exists()
		|| wallpaperFile.length() == 0
		|| savedTime == 0
		|| cacheAge >= maxCacheTime;
		
		
		// ============================================================
		// BUSCA NOVA PAISAGEM
		// ============================================================
		
		if (internetAvailable
		&& needNewWallpaper) {
			
			final boolean useMobileNetwork =
			mobileNetwork;
			
			
			new android.os.AsyncTask<
			Void,
			Void,
			android.graphics.Bitmap>() {
				
				
				@Override
				protected android.graphics.Bitmap
				doInBackground(Void... params) {
					
					java.net.HttpURLConnection
					apiConnection = null;
					
					
					java.net.HttpURLConnection
					imageConnection = null;
					
					
					java.io.InputStream
					apiInput = null;
					
					
					java.io.InputStream
					imageInput = null;
					
					
					java.io.FileOutputStream
					fileOutput = null;
					
					
					try {
						
						// ====================================================
						// TAMANHO DA IMAGEM
						// ====================================================
						
						int thumbnailWidth;
						
						
						if (useMobileNetwork) {
							
							// Dados móveis
							thumbnailWidth = 540;
							
						} else {
							
							// Wi-Fi
							thumbnailWidth = 1080;
						}
						
						
						// ====================================================
						// TERMOS DE PESQUISA
						//
						// São escolhidos aleatoriamente para variar
						// as paisagens.
						// ====================================================
						
						String[] searches = {
							
							"landscape",
							"nature landscape",
							"mountain landscape",
							"forest landscape",
							"lake landscape",
							"beach landscape",
							"sunset landscape",
							"river landscape",
							"waterfall landscape",
							"countryside landscape"
							
						};
						
						
						java.util.Random random =
						new java.util.Random();
						
						
						String search =
						searches[
						random.nextInt(
						searches.length
						)
						];
						
						
						// ====================================================
						// API WIKIMEDIA COMMONS
						// ====================================================
						
						String apiUrl =
						"https://commons.wikimedia.org/w/api.php"
						+ "?action=query"
						+ "&format=json"
						+ "&generator=search"
						+ "&gsrsearch="
						+ java.net.URLEncoder.encode(
						search,
						"UTF-8"
						)
						+ "&gsrnamespace=6"
						+ "&gsrlimit=20"
						+ "&prop=imageinfo"
						+ "&iiprop=url|dimensions"
						+ "&iiurlwidth="
						+ thumbnailWidth;
						
						
						java.net.URL apiURL =
						new java.net.URL(
						apiUrl
						);
						
						
						apiConnection =
						(java.net.HttpURLConnection)
						apiURL.openConnection();
						
						
						apiConnection.setRequestMethod(
						"GET"
						);
						
						
						apiConnection.setConnectTimeout(
						10000
						);
						
						
						apiConnection.setReadTimeout(
						15000
						);
						
						
						apiConnection.setRequestProperty(
						"User-Agent",
						"SketchwarePro-Landscape/1.0"
						);
						
						
						apiConnection.setRequestProperty(
						"Api-User-Agent",
						"SketchwarePro-Landscape/1.0"
						);
						
						
						apiInput =
						apiConnection.getInputStream();
						
						
						java.io.ByteArrayOutputStream
						apiOutput =
						new java.io.ByteArrayOutputStream();
						
						
						byte[] apiBuffer =
						new byte[4096];
						
						
						int apiLength;
						
						
						while ((apiLength =
						apiInput.read(apiBuffer)) != -1) {
							
							apiOutput.write(
							apiBuffer,
							0,
							apiLength
							);
						}
						
						
						String json =
						new String(
						apiOutput.toByteArray(),
						"UTF-8"
						);
						
						
						// ====================================================
						// JSON
						// ====================================================
						
						org.json.JSONObject root =
						new org.json.JSONObject(
						json
						);
						
						
						if (!root.has("query")) {
							return null;
						}
						
						
						org.json.JSONObject query =
						root.getJSONObject(
						"query"
						);
						
						
						if (!query.has("pages")) {
							return null;
						}
						
						
						org.json.JSONObject pages =
						query.getJSONObject(
						"pages"
						);
						
						
						java.util.ArrayList<String>
						validPages =
						new java.util.ArrayList<String>();
						
						
						java.util.Iterator<String>
						iterator =
						pages.keys();
						
						
						// ====================================================
						// FILTRA SOMENTE PAISAGENS HORIZONTAIS
						// ====================================================
						
						while (iterator.hasNext()) {
							
							String key =
							iterator.next();
							
							
							try {
								
								org.json.JSONObject page =
								pages.getJSONObject(
								key
								);
								
								
								if (!page.has(
								"imageinfo"
								)) {
									continue;
								}
								
								
								org.json.JSONArray imageInfo =
								page.getJSONArray(
								"imageinfo"
								);
								
								
								if (imageInfo.length() == 0) {
									continue;
								}
								
								
								org.json.JSONObject info =
								imageInfo.getJSONObject(
								0
								);
								
								
								if (!info.has("width")
								|| !info.has("height")) {
									
									continue;
								}
								
								
								int originalWidth =
								info.getInt(
								"width"
								);
								
								
								int originalHeight =
								info.getInt(
								"height"
								);
								
								
								if (originalHeight <= 0) {
									continue;
								}
								
								
								// ------------------------------------------------
								// PROPORÇÃO
								//
								// 1.25 ou maior = horizontal
								// ------------------------------------------------
								
								float ratio =
								(float)
								originalWidth
								/
								(float)
								originalHeight;
								
								
								// Evita imagens verticais/quadradas
								if (ratio >= 1.25f
								&& originalWidth >= 1000
								&& originalHeight >= 500) {
									
									validPages.add(
									key
									);
								}
								
							} catch (Exception ignored) {
							}
						}
						
						
						// ====================================================
						// NÃO ENCONTROU PAISAGEM
						// ====================================================
						
						if (validPages.size() == 0) {
							return null;
						}
						
						
						// ====================================================
						// ESCOLHE UMA PAISAGEM
						// ====================================================
						
						String selectedKey =
						validPages.get(
						random.nextInt(
						validPages.size()
						)
						);
						
						
						org.json.JSONObject selectedPage =
						pages.getJSONObject(
						selectedKey
						);
						
						
						org.json.JSONArray imageInfo =
						selectedPage.getJSONArray(
						"imageinfo"
						);
						
						
						org.json.JSONObject info =
						imageInfo.getJSONObject(
						0
						);
						
						
						// ====================================================
						// URL DO THUMBNAIL
						// ====================================================
						
						String imageUrl = null;
						
						
						if (info.has("thumburl")) {
							
							imageUrl =
							info.getString(
							"thumburl"
							);
							
						} else if (info.has("url")) {
							
							imageUrl =
							info.getString(
							"url"
							);
						}
						
						
						if (imageUrl == null
						|| imageUrl.length() == 0) {
							
							return null;
						}
						
						
						// ====================================================
						// DOWNLOAD DA PAISAGEM
						// ====================================================
						
						java.net.URL imageURL =
						new java.net.URL(
						imageUrl
						);
						
						
						imageConnection =
						(java.net.HttpURLConnection)
						imageURL.openConnection();
						
						
						imageConnection.setRequestMethod(
						"GET"
						);
						
						
						imageConnection.setConnectTimeout(
						10000
						);
						
						
						imageConnection.setReadTimeout(
						20000
						);
						
						
						imageConnection.setRequestProperty(
						"User-Agent",
						"SketchwarePro-Landscape/1.0"
						);
						
						
						imageInput =
						imageConnection.getInputStream();
						
						
						android.graphics.Bitmap bitmap =
						android.graphics.BitmapFactory
						.decodeStream(
						imageInput
						);
						
						
						if (bitmap == null) {
							return null;
						}
						
						
						// ====================================================
						// SALVA NO CACHE
						// ====================================================
						
						fileOutput =
						new java.io.FileOutputStream(
						wallpaperFile
						);
						
						
						bitmap.compress(
						android.graphics.Bitmap.CompressFormat.JPEG,
						80,
						fileOutput
						);
						
						
						fileOutput.flush();
						
						
						fileOutput.close();
						
						
						fileOutput = null;
						
						
						// ====================================================
						// SALVA HORÁRIO
						// ====================================================
						
						java.io.FileWriter writer =
						new java.io.FileWriter(
						wallpaperTimeFile
						);
						
						
						writer.write(
						String.valueOf(
						System.currentTimeMillis()
						)
						);
						
						
						writer.close();
						
						
						return bitmap;
						
						
					} catch (Exception e) {
						
						return null;
						
						
					} finally {
						
						try {
							
							if (apiInput != null) {
								apiInput.close();
							}
							
						} catch (Exception e) {
						}
						
						
						try {
							
							if (imageInput != null) {
								imageInput.close();
							}
							
						} catch (Exception e) {
						}
						
						
						try {
							
							if (fileOutput != null) {
								fileOutput.close();
							}
							
						} catch (Exception e) {
						}
						
						
						if (apiConnection != null) {
							apiConnection.disconnect();
						}
						
						
						if (imageConnection != null) {
							imageConnection.disconnect();
						}
					}
				}
				
				
				// ========================================================
				// EXIBE NOVA PAISAGEM
				// ========================================================
				
				@Override
				protected void onPostExecute(
				android.graphics.Bitmap bitmap) {
					
					if (bitmap != null) {
						
						currentBitmap[0] =
						bitmap;
						
						
						LandscapeDrawable drawable =
						new LandscapeDrawable(
						bitmap
						);
						
						
						wallpaperLayout
						.setBackgroundDrawable(
						drawable
						);
					}
				}
				
			}.execute();
		}
	}
	
	
	public void _armazenamento() {
		//PARA O ARMAZENAMENTO DE DADOS
		//brilho da tela
		if (Dados.getString("brilho", "").equals("ativado")) {
			checkbox_brilho.setChecked(true);
			webview.setAlpha((float)(0.5d));
		} else {
			
		}
		if (Dados.getString("brilho", "").equals("desativado")) {
			checkbox_brilho.setChecked(false);
			webview.setAlpha((float)(1.0d));
		} else {
			
		}
		//javascript
		if (Dados.getString("java", "").equals("ativado")) {
			checkbox_javascript2.setChecked(true);
			webview.getSettings().setJavaScriptEnabled(true); 
		} else {
			
		}
		if (Dados.getString("java", "").equals("desativado")) {
			checkbox_javascript2.setChecked(false);
			webview.getSettings().setJavaScriptEnabled(false); 
		} else {
			
		}
		//vazio
		if (Dados.getString("vazio", "").equals("desativado")) {
			webview.setWebViewClient(new android.webkit.WebViewClient() {
				@Override
				public void onPageFinished(android.webkit.WebView view, String url) {
					super.onPageFinished(view, url);
					
					String u = view.getUrl();
					
					if (u == null || u.equals("") || u.equals("about:blank")) {
						vazio.setVisibility(android.view.View.VISIBLE);
					} else {
						vazio.setVisibility(android.view.View.GONE);
					}
				}
			});
		} else {
			
		}
	}
	
	
	public void _criação() {
		
	}
	
	
	public void _download() {
		webview.setDownloadListener(new DownloadListener() {
			@Override
			public void onDownloadStart(final String url, final String userAgent, final String contentDisposition, final String mimetype, long contentLength) {
				final String fileName = URLUtil.guessFileName(url, contentDisposition, mimetype);
				
				// Verifica permissão no Sketchware/Android
				if (android.os.Build.VERSION.SDK_INT >= 23 && android.os.Build.VERSION.SDK_INT <= 28) {
					if (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == android.content.pm.PackageManager.PERMISSION_DENIED) {
						requestPermissions(new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1000);
					}
				}
				
				AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
				builder.setTitle("Download");
				builder.setMessage("Deseja baixar o arquivo " + fileName + "?");
				builder.setPositiveButton("Sim", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						try {
							DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
							request.setTitle(fileName);
							request.setDescription("Baixando...");
							request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
							request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName);
							
							// Adiciona UserAgent e Cookies para evitar erros de 403 Forbidden
								String cookie = android.webkit.CookieManager.getInstance().getCookie(url);
							if (cookie != null && !cookie.trim().isEmpty()) {
								request.addRequestHeader("Cookie", cookie);
							}
							if (userAgent != null && !userAgent.trim().isEmpty()) {
								request.addRequestHeader("User-Agent", userAgent);
							}
							
							DownloadManager dm = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
							dm.enqueue(request);
							android.widget.Toast.makeText(getApplicationContext(), "Download iniciado...", android.widget.Toast.LENGTH_SHORT).show();
						} catch (Exception e) {
							android.widget.Toast.makeText(getApplicationContext(), "Erro: " + e.getMessage(), android.widget.Toast.LENGTH_SHORT).show();
						}
					}
				});
				builder.setNegativeButton("Não", null);
				
				// Estilo Customizado (Bordas 20, Fundo 34495E, Botões 9E9E9E)
				final AlertDialog d = builder.create();
				d.show();
				
				android.graphics.drawable.GradientDrawable gd = new android.graphics.drawable.GradientDrawable();
				gd.setColor(0xFF34495E);
				gd.setCornerRadius(20);
				d.getWindow().setBackgroundDrawable(gd);
				d.getWindow().setDimAmount(0.7f);
				
				d.getButton(DialogInterface.BUTTON_POSITIVE).setTextColor(0xFF9E9E9E);
				d.getButton(DialogInterface.BUTTON_NEGATIVE).setTextColor(0xFF9E9E9E);
				
				// Cor do texto para contraste
				android.widget.TextView tvMsg = (android.widget.TextView) d.findViewById(android.R.id.message);
				if (tvMsg != null) tvMsg.setTextColor(android.graphics.Color.WHITE);
				
				int titleId = getResources().getIdentifier("alertTitle", "id", "android");
				if (titleId > 0) {
					android.widget.TextView tvTitle = (android.widget.TextView) d.findViewById(titleId);
					tvTitle.setTextColor(android.graphics.Color.WHITE);
				}
			}
		});
		
	}
	
	
	@Deprecated
	public void showMessage(String _s) {
		Toast.makeText(getApplicationContext(), _s, Toast.LENGTH_SHORT).show();
	}
	
	@Deprecated
	public int getLocationX(View _v) {
		int _location[] = new int[2];
		_v.getLocationInWindow(_location);
		return _location[0];
	}
	
	@Deprecated
	public int getLocationY(View _v) {
		int _location[] = new int[2];
		_v.getLocationInWindow(_location);
		return _location[1];
	}
	
	@Deprecated
	public int getRandom(int _min, int _max) {
		Random random = new Random();
		return random.nextInt(_max - _min + 1) + _min;
	}
	
	@Deprecated
	public ArrayList<Double> getCheckedItemPositionsToArray(ListView _list) {
		ArrayList<Double> _result = new ArrayList<Double>();
		SparseBooleanArray _arr = _list.getCheckedItemPositions();
		for (int _iIdx = 0; _iIdx < _arr.size(); _iIdx++) {
			if (_arr.valueAt(_iIdx))
			_result.add((double)_arr.keyAt(_iIdx));
		}
		return _result;
	}
	
	@Deprecated
	public float getDip(int _input) {
		return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, _input, getResources().getDisplayMetrics());
	}
	
	@Deprecated
	public int getDisplayWidthPixels() {
		return getResources().getDisplayMetrics().widthPixels;
	}
	
	@Deprecated
	public int getDisplayHeightPixels() {
		return getResources().getDisplayMetrics().heightPixels;
	}
}