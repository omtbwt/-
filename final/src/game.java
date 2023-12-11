import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
	
public class game extends JFrame implements ActionListener {
	    private JPanel contentPane;
	    protected JTextField textField;
	    private game_Frame gameFrame;
	    protected JTextArea textArea; 
	    
	    public static void main(String[] args) {
	        SwingUtilities.invokeLater(() -> {
	            try {
	                game frame = new game();
	                frame.setVisible(true);
	                SwingWorker<Void, Void> worker = new SwingWorker<>() {
	                    @Override
	                    protected Void doInBackground() throws Exception {
	                        frame.gameFrame.MessengerMulti();
	                        return null;
	                    }
	                };
	                worker.execute();
	            } catch (Exception e) {
	                e.printStackTrace();
	            }
	        });
	    }
	
	    public game() {
	        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	        setBounds(100, 100, 1280, 720);
	
	        contentPane = new JPanel();
	        contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
	        setContentPane(contentPane);
	        contentPane.setLayout(null);
	
	        gameFrame = new game_Frame(this); // GameFrame 인스턴스 생성
	        gameFrame.setBounds(12, 22, 954, 632);
	        contentPane.add(gameFrame);
	
	        textField = new JTextField();
	        textField.setBounds(978, 633, 274, 21);
	        contentPane.add(textField);
	        textField.setColumns(10);
	
	        textArea = new JTextArea();
	        textArea.setBounds(978, 363, 274, 261);
	        textArea.setEditable(false);
	        contentPane.add(textArea);
	       
	        textField.addActionListener(this);	        	
	        
	    }
	    @Override
        public void actionPerformed(ActionEvent evt) {
            String message = textField.getText();

            gameFrame.sendMessage("message:" + message);

            textArea.append("나: " + message + "\n");
            textField.setText(""); // 입력 필드 지우기
            gameFrame.setFocusable(true);
            gameFrame.requestFocusInWindow();

        }
	    
	}
	
	class game_Frame extends JPanel implements KeyListener, Runnable {
	
		private game parent;
		private BufferedImage backgroundImage;
		
	    final static int ServerPort = 5006;
	    DataInputStream is;
	    DataOutputStream os;
	    int num; //p1 p2 구분
	
	    int f_width = 900;
	    int f_height = 650;
	
	    int x_p1, y_p1;
	    int x_p2, y_p2;
	
	    int lives_p1 = 3; // p1의 목숨
	    int lives_p2 = 3; // p2의 목숨
	
	    boolean KeyUp = false;
	    boolean KeyDown = false;
	    boolean KeyLeft = false;
	    boolean KeyRight = false;
	    boolean KeySpace = false;//미사일 발사

	    private boolean reGame = false; // 상대방의 재시작 여부
	    
	    Toolkit tk = Toolkit.getDefaultToolkit();
	    //p1
	    Image originalMeImg = tk.getImage("image/airplane1.png");
	    Image me_img = originalMeImg.getScaledInstance(120, 120, Image.SCALE_SMOOTH);
	    Image buffImage;
	    Graphics buffg;
	    //p2
	    Image originalMeImgP2 = tk.getImage("image/airplane2.png");
	    Image me_img_p2 = originalMeImgP2.getScaledInstance(120, 120, Image.SCALE_SMOOTH);
	
	    Image originalMeImg3 = tk.getImage("image/Missile1.png"); //미사일 이미지 변수
	    Image originalMeImg4 = tk.getImage("image/Missile2.png");
	    Image Missile_p1_img = originalMeImg3.getScaledInstance(50, 50, Image.SCALE_SMOOTH);
	    Image Missile_p2_img = originalMeImg4.getScaledInstance(50, 50, Image.SCALE_SMOOTH);
	    ArrayList<Missile> Missile_List_p1 = new ArrayList<Missile>();
	    ArrayList<Missile> Missile_List_p2 = new ArrayList<Missile>();
	
	    Missile ms; // 미사일 클래스 접근 키
	
	    Thread th;
	
	    game_Frame(game parent) {
	        this.parent = parent;
	        init();
	        start();
	        this.setBackground(Color.white);
	        
	        Dimension screen = tk.getScreenSize();
	    
	        int f_xpos = (int) (screen.getWidth() / 2 - f_width / 2);
	        int f_ypos = (int) (screen.getHeight() / 2 - f_height / 2);
	    
	        setLocation(f_xpos, f_ypos);
	        
	        addKeyListener(this);
	        setFocusable(true);
	        setFocusTraversalKeysEnabled(false);
	        
	        // 배경 이미지 로드
	        try {
	            backgroundImage = ImageIO.read(new File("image/background.jpg"));
	            
	            int width = 1000;
	            int height = 600;

	            // 배경 이미지 크기 조정
	            Image resizedImage = backgroundImage.getScaledInstance(width, height, Image.SCALE_SMOOTH);

	            // Image를 BufferedImage로 변환
	            BufferedImage bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
	            Graphics2D g2d = bufferedImage.createGraphics();
	            g2d.drawImage(resizedImage, 0, 0, null);
	            g2d.dispose();

	            backgroundImage = bufferedImage;
	         
	        } catch (IOException e) {
	            e.printStackTrace();
	        }
	    }
	
	    public void sendMessage(String message) {
			// TODO Auto-generated method stub
	    	try {
		    	os.writeUTF("message:" + message);
		        os.flush(); // 버퍼 비우기
	    	} catch (IOException e) {
                e.printStackTrace();
            }
	    	
			
		}

		class Missile{ // 미사일 위치 파악 및 이동을 위한 클래스 추가 
	
	    	Point pos; //미사일 좌표 변수
	    	Missile(int x, int y){ //미사일 좌표를 입력 받는 메소드
	    		pos = new Point(x, y); //미사일 좌표를 체크
	    	}
	    	public void move(int i){ //미사일 이동을 위한 메소드
	    		if(i==0)
	    			pos.x += 10; //x 좌표에 10만큼 미사일 이동 p1
	    		else if (i==1)
	    			pos.x -= 10; //x 좌표에 -10만큼 미사일 이동 p2
	    	}
	    }
	
	    public void init() { 
	
	    	x_p1 = 100;
	    	y_p1 = 100;
	
	    	x_p2 = 700;
	    	y_p2 = 100;
	
	    }
	
	    public void start() {
	        th = new Thread(this);
	        th.start();
	    }
	
	    public void run() {
	        try {
	            while (true) {
	                KeyProcess();
	                MissileProcess();
	                if (checkCollision()) { // 미사일 충돌 체크
	                    if (num == 0) {
	                        lives_p1--; // p1의 목숨 감소
	                    } else if (num == 1) {
	                        lives_p2--; // p2의 목숨 감소
	                    }
	                    if (lives_p1 == 0 || lives_p2 == 0) {
	                        // 게임이 종료되면 팝업 알림을 표시
	                        String winner = (lives_p1 == 0) ? "플레이어 1" : "플레이어 2";
	                        showGameOverPopup(winner);
	                    }
	                }
	                repaint();
	                Thread.sleep(20);            
	              }
	        } catch (Exception e) {
	            e.printStackTrace();
	        }
	    }
	    
	    private void showGameOverPopup(String winner) {
	        Object[] options = {"나가기", "재시작"};

	        int result = JOptionPane.showOptionDialog(this,
	                "게임 종료! " + winner + " 승리.",
	                "게임 종료",
	                JOptionPane.YES_NO_OPTION,
	                JOptionPane.INFORMATION_MESSAGE,
	                null,
	                options,
	                options[1]);

	        if (result == JOptionPane.YES_OPTION) {
	        	try {
                    os.writeUTF("exit"); // 프로그램 종료 알리기
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
	            System.exit(0); // 프로그램 종료
	        } else if (result == JOptionPane.NO_OPTION) {
	            restartGame(); // 게임 재시작
	        }
	    }
	    
	    private void restartGame() {
	    	synchronized (this) {
	            Thread restartThread = new Thread(() -> {
	                while (true) {
	                    if (reGame) {
	                        break;
	                    }
	                }
	            });

	    	// 스레드 시작
	    	restartThread.start();
	    	reGame = false;
	        // 게임 상태를 초기값으로 재설정
	        x_p1 = 100;
	        y_p1 = 100;

	        x_p2 = 700;
	        y_p2 = 100;

	        lives_p1 = 3;
	        lives_p2 = 3;

	        Missile_List_p1.clear();
	        Missile_List_p2.clear();

	        // 추가적인 초기화가 필요한 경우 여기에 추가
	        
	        
	        // 게임 스레드 재시작
	        th.interrupt();
	        start();
	        	}
	        }
	    
	
	    private boolean checkCollision() {
	        // 삭제할 미사일 목록
	        List<Missile> missilesToRemoveP1 = new ArrayList<>();
	        List<Missile> missilesToRemoveP2 = new ArrayList<>();

	        // p1과 p2의 미사일과의 충돌 여부 체크
	        Iterator<Missile> iteratorP1 = Missile_List_p1.iterator();
	        while (iteratorP1.hasNext()) {
	            Missile missile = iteratorP1.next();
	            if (missile.pos.x + 50 >= x_p2 && missile.pos.x <= x_p2 + me_img_p2.getWidth(null)
	                    && missile.pos.y + 30 >= y_p2 && missile.pos.y <= y_p2 + me_img_p2.getHeight(null)) {
	                iteratorP1.remove(); // 충돌한 미사일 리스트에서 즉시 제거
	                lives_p2--; // p2의 목숨 감소
	                if (lives_p2 == 0) {
	                    showGameOverPopup("플레이어 1"); // 플레이어 1 승리
	                }
	            }
	        }

	        Iterator<Missile> iteratorP2 = Missile_List_p2.iterator();
	        while (iteratorP2.hasNext()) {
	            Missile missile = iteratorP2.next();
	            if (missile.pos.x >= x_p1 && missile.pos.x <= x_p1 + me_img.getWidth(null)
	                    && missile.pos.y + 30 >= y_p1 && missile.pos.y <= y_p1 + me_img.getHeight(null)) {
	                iteratorP2.remove(); // 충돌한 미사일 리스트에서 즉시 제거
	                lives_p1--; // p1의 목숨 감소
	                if (lives_p1 == 0) {
	                    showGameOverPopup("플레이어 2"); // 플레이어 2 승리
	                }
	            }
	        }

	        // 충돌한 미사일 삭제
	        Missile_List_p1.removeAll(missilesToRemoveP1);
	        Missile_List_p2.removeAll(missilesToRemoveP2);

	        // 충돌 여부 반환
	        return !missilesToRemoveP1.isEmpty() || !missilesToRemoveP2.isEmpty();
	    }
	
	    public void paint(Graphics g) {
	    	
	        buffImage = createImage(f_width, f_height);
	        buffg = buffImage.getGraphics();
	        
	        Draw_Char();
	        Draw_Missile();
	        g.drawImage(buffImage, 0, 0, this);
	        
	    }

	    public void update(Graphics g) {
	  
	    	buffImage = createImage(f_width, f_height);
	        buffg = buffImage.getGraphics();
	        
	        Draw_Char();
	        Draw_Missile();
	        g.drawImage(buffImage, 0, 0, this);
	        
	    }
	
	    public void MissileProcess() {
	    	
	        if (KeySpace) {
	        // 스페이스바 키가 눌렸을 때만 미사일 객체 생성
	            if (num == 0) {
	            	
	                ms = new Missile(x_p1, y_p1);
	                Missile_List_p1.add(ms); // 해당 미사일 추가

	                // 서버로 미사일 정보 전송
	                try {
	                    os.writeUTF("M_p1"); // p1의 미사일 발사 알리기
	                } catch (IOException ex) {
	                    ex.printStackTrace();
	                }

	                KeySpace = false; // 발사 후 키 상태 초기화
	            }
	            
	            if (num == 1) {
	            	
	                ms = new Missile(x_p2, y_p2);
	                Missile_List_p2.add(ms); // 해당 미사일 추가

	                // 서버로 미사일 정보 전송
	                try {
	                    os.writeUTF("M_p2"); // p2의 미사일 발사 알리기
	                } catch (IOException ex) {
	                    ex.printStackTrace();
	                }

	                KeySpace = false; // 발사 후 키 상태 초기화
	            }
	        }
	    }
	
	    public void Draw_Char() {
	    	
	        buffg.clearRect(0, 0, f_width, f_height);
	        buffg.drawImage(backgroundImage, 0, 0, this);
	        
	        buffg.drawImage(me_img, x_p1, y_p1, this);
	        buffg.drawImage(me_img_p2, x_p2, y_p2, this);
	        
	    }
	
	    public void keyPressed(KeyEvent e) {
	    	
	            switch (e.getKeyCode()) {
	            
	                case KeyEvent.VK_UP:
	                    KeyUp = true;
	                    break;
	                case KeyEvent.VK_DOWN:
	                    KeyDown = true;
	                    break;
	                case KeyEvent.VK_LEFT:
	                    KeyLeft = true;
	                    break;
	                case KeyEvent.VK_RIGHT:
	                    KeyRight = true;
	                    break;
	                case KeyEvent.VK_SPACE : // 스페이스키 입력 처리 추가
	                	KeySpace = true;
	                	break;
	                case KeyEvent.VK_ENTER: //메시지를 보내기위해
	                    parent.textField.requestFocus();  // 텍스트 입력 상태로 전환
	                    break;
	            }
	        }
	
	
	    public void Draw_Missile(){ // 미사일 그리는 메소드
	    	
	    	//p1
	    	for (int i = 0 ; i < Missile_List_p1.size()  ; ++i){
	    	//미사일 존재 유무를 확인한다.
		
	    		ms = (Missile) (Missile_List_p1.get(i)); 
			    // 미사일 위치값을 확인
			
	    		buffg.drawImage(Missile_p1_img, ms.pos.x + 150, ms.pos.y + 30, this); 
			    // 현재 좌표에 미사일 그리기.
			    // 이미지 크기를 감안한 미사일 발사 좌표는 수정됨.
			
	    		ms.move(0);
			    // 그려진 미사일을 정해진 숫자만큼 이동시키기
			
	    		if ( ms.pos.x > f_width || ms.pos.x < 0 ){ // 미사일이 화면 밖으로 나가면
	    			Missile_List_p1.remove(i); // 미사일 지우기
	    		}
	    	}
	    	
	    	//p2
	    	for (int i = 0 ; i < Missile_List_p2.size()  ; ++i){
	    	
	    		ms = (Missile) (Missile_List_p2.get(i));     		
	    		buffg.drawImage(Missile_p2_img, ms.pos.x, ms.pos.y + 30, this); 
	    		ms.move(1);
	    		
	    		if ( ms.pos.x > f_width ){ 
	    			Missile_List_p2.remove(i);
	    		}
	    	}
	    	
	    }
	
	    public void keyReleased(KeyEvent e) {
	
	            switch (e.getKeyCode()) {
	                case KeyEvent.VK_UP:
	                    KeyUp = false;
	                    break;
	                case KeyEvent.VK_DOWN:
	                    KeyDown = false;
	                    break;
	                case KeyEvent.VK_LEFT:
	                    KeyLeft = false;
	                    break;
	                case KeyEvent.VK_RIGHT:
	                    KeyRight = false;
	                    break;
	                case KeyEvent.VK_SPACE : // 스페이스키 입력 처리 추가
	                	KeySpace = false;
	                	break;
	            }
	    }
	
	    public void keyTyped(KeyEvent e) {
	    }
	
	    public void KeyProcess() {
	        if(num==0) {//p1일때
		    	int newX = x_p1;
		        int newY = y_p1;
	
		        if (KeyUp) newY -= 5;
		        if (KeyDown) newY += 5;
		        if (KeyLeft) newX -= 5;
		        if (KeyRight) newX += 5;
	
		        // 값이 변경되었을 때만 전송
		        if (newX != x_p1 || newY != y_p1) {
		            try {
		                os.writeUTF("X_p1");
		                os.writeInt(newX);
		                os.writeUTF("Y_p1");
		                os.writeInt(newY);
		                os.flush();
	
		                x_p1 = newX;
		                y_p1 = newY;
		            } catch (IOException e) {
		                e.printStackTrace();
		            }
		        }
	        }
	        else if(num==1) {//p2일때
	        	int newX = x_p2;
		        int newY = y_p2;
	
		        if (KeyUp) newY -= 5;
		        if (KeyDown) newY += 5;
		        if (KeyLeft) newX -= 5;
		        if (KeyRight) newX += 5;
	
		        if (newX != x_p2 || newY != y_p2) {
		            try {
		                os.writeUTF("X_p2");
		                os.writeInt(newX);
		                os.writeUTF("Y_p2");
		                os.writeInt(newY);
		                os.flush();
	
		                x_p2 = newX;
		                y_p2 = newY;
		            } catch (IOException e) {
		                e.printStackTrace();
		            }
		        }
	        }
	    }
	
	    public void MessengerMulti() {
	    	
	        try {
	            InetAddress ip = InetAddress.getByName("localhost");
	            Socket s = new Socket(ip, ServerPort);
	            is = new DataInputStream(s.getInputStream());
	            os = new DataOutputStream(s.getOutputStream());
	
	            try {
	            	String messageType = is.readUTF();
	            	if (messageType.equals("Server is full.")) {
                        // 서버가 가득 찼음을 나타내는 메시지를 받았을 때의 처리
                    	JOptionPane.showMessageDialog(null, "Server is full. Cannot connect.", "Error", JOptionPane.ERROR_MESSAGE);
                        // 프로그램 종료
                    	System.exit(0);
                    }
	            	num = is.readInt();
	            }
	            catch (IOException e) {
	                e.printStackTrace();
	            }
	
	            Thread thread2 = new Thread(() -> {
	                while (true) {
	                    try {
	                    	
	                        String messageType = is.readUTF();
	                        
	                        if (messageType.equals("X_p1")) {
	                            // 정수값 x 수신
	                            x_p1 = is.readInt();
	                        } 
	                        else if (messageType.equals("Y_p1")) {
	                            // 정수값 y 수신
	                            y_p1 = is.readInt();
	                        }
	                        else if (messageType.equals("X_p2")) {
	                            // 정수값 y 수신
	                            x_p2 = is.readInt();
	                        }
	                        else if (messageType.equals("Y_p2")) {
	                            // 정수값 y 수신
	                            y_p2 = is.readInt();
	                        }
	                        else if (messageType.equals("M_p1")) {
	                        	// 미사일 생성
	                        	ms = new Missile(x_p1, y_p1);
	                        	Missile_List_p1.add(ms);
	                        }
	                        else if (messageType.equals("M_p2")) {
	                        	ms = new Missile(x_p2, y_p2);
	                        	Missile_List_p2.add(ms);;
	                        }
	                        else if (messageType.startsWith("message:")) {
	                            // 메시지를 추출하고 textArea에 표시
	                            String message = messageType.substring("message:".length());
	                            displayMessage("상대방 " + message);
	                        }
	                        else if (messageType.equals("exit")) {
	                            // 상대방의 종료 수신하기
	                        	JOptionPane.showOptionDialog(
	                                    null,
	                                    "상대방이 게임을 종료했습니다. 게임을 종료하시겠습니까?",
	                                    "알림",
	                                    JOptionPane.DEFAULT_OPTION,
	                                    JOptionPane.INFORMATION_MESSAGE,
	                                    null,  // 아이콘 없음
	                                    new Object[]{"게임 종료"},  // 버튼 텍스트
	                                    "게임 종료"  // 기본 선택 버튼
	                            );
	                        	System.exit(0);
	                        }
	
	                    } catch (IOException e) {
	                        e.printStackTrace();
	                    }
	                }
	            });
	            thread2.start();
	        } catch (IOException e) {
	            e.printStackTrace();
	        }
	    }
	    private void displayMessage(String message) {
	        SwingUtilities.invokeLater(() -> {
	        	parent.textArea.append(message + "\n");
	        });
	    }
	}