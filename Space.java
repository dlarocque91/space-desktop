import java.awt.*;
import javax.swing.*;
import java.awt.event.*;
import java.awt.image.*;
import java.util.Random;
import java.util.concurrent.*;

public class Space extends Canvas implements KeyListener,Runnable{

    final static int HEIGHT = Toolkit.getDefaultToolkit().getScreenSize().height;
    final static int WIDTH = Toolkit.getDefaultToolkit().getScreenSize().width;
	final int  TARGET_AMOUNT = 25;
    Player player;
	Boss boss;
	Thread game;
	Random gen = new Random();
	CopyOnWriteArrayList<Bullet> bullets = new CopyOnWriteArrayList<Bullet>();
	CopyOnWriteArrayList<Alien> aliens = new CopyOnWriteArrayList<Alien>();
	CopyOnWriteArrayList<Bonus> bonuses = new CopyOnWriteArrayList<Bonus>();
	CopyOnWriteArrayList<Explosion> explosions = new CopyOnWriteArrayList<Explosion>();
	boolean leftPressed = false;
	boolean rightPressed = false;
	boolean upPressed = false;
	boolean downPressed = false;
	boolean shootPressed = false;
	boolean canShoot = false;
	boolean isPaused = false;
	boolean gameRunning = false;
	boolean gameOver = false;
	boolean levelDone = false;
	boolean youWin = false;
	boolean youLose = false;
	boolean canCreateBonus = false;
	boolean explosionHappened = false;
	boolean bossReached = false;
	boolean bossReady = false;
	boolean singleShot = false;
	boolean triShot = true;
	boolean fiveShot = false;
	long shootTime = 300;
	long lastShotTime = 0;
	long alienTime = 1000;
	long lastAlienTime = 0;
	static int score = 0;
	static int highScore = 0;
	int remEnemies = TARGET_AMOUNT;
	int bonusX,bonusY;
	int explX,explY;
	int gunState = 0;
	int bonusTime = 5;
	static int level = 1;
	BufferStrategy buffer;
	Graphics2D g;
	Image background;
	int backgroundX,backgroundY;
	
	public Space() {
	
	  JFrame window = new JFrame(" Space Shooter");
	  window.add("Center",this);
	  window.setDefaultCloseOperation(3);
	  window.setSize(WIDTH,HEIGHT);
	  window.setIconImage(new ImageIcon("ship.png").getImage());
	  window.setVisible(true);
	  player = new Player(384,600,100,"ship.png");
	  boss = new Boss(100,20,400,"alien.gif");
	  setIgnoreRepaint(true);
	 
	  createBufferStrategy(3);
	  buffer = getBufferStrategy();
	  
	  background = new ImageIcon("background.jpg").getImage();
	  
	  startGame(); 	
	}
	
	public void init() {
	   addKeyListener(this);	
	   requestFocus();
	   remEnemies = TARGET_AMOUNT;
	   lastShotTime = 0;
	   alienTime = 1000;
	   level = 1;
	   score = 0;
	   gunState = 0;
	   bonusTime = 5;
	   backgroundX = 0;
	   backgroundY = -background.getHeight(null)/4;
	   singleShot = true;
	   triShot = false;
	   fiveShot = false;
	   player.isDead = false;
	   boss.isDead = false;
	   isPaused = false;
	   bossReached = false;
	   bossReady = false;
	   explosionHappened = false;
	   youWin = false;
	   youLose = false;
	   gameOver = false;
	   levelDone = false;
	   player.shieldTime = 0;
	   player.setHealth(100);
	   boss.setHealth(400);
	   boss.difficulty = 1;
	   player.setX(383);
	   player.setY(512);
	}
	
	public void startGame() {
	   if (game == null || !gameRunning)
	   {
	       game = new Thread(this);
		   game.start();
	   }
	}
	
	public void stopGame() {
	  removeKeyListener(this);
	  gameRunning = false;
	}
	
	public void run() {
	   init();
	   gameRunning = true; 
       while(gameRunning) {
	      if(!gameOver) {
	        loop();
		  } else {
		    gameOver();
		  }
		  
		  try{
	        Thread.sleep(20);
	      }catch(Exception ex){}
	   }

       System.exit(0);	   
	}
	
	public void loop() {
	  g = (Graphics2D) buffer.getDrawGraphics();
	  // args = image, x, y, sizeX, sizeY, obverser
	  g.drawImage(background,backgroundX,backgroundY,WIDTH,HEIGHT*2,null);
	  if(!levelDone) {
	    if(!isPaused) {
	     update();   
	    }
	  
	    render();
	  } else {
	    levelDone();
		try{
		 Thread.sleep(2000);
		}catch(Exception e){}
	  }
	  
	  g.dispose();
	  buffer.show();
	}
	
	public void keyPressed(KeyEvent evt) {
	 int key = evt.getKeyCode();

      if (key == KeyEvent.VK_RIGHT) {
	    rightPressed = true;
	  } 	

      if (key == KeyEvent.VK_LEFT) {
	    leftPressed = true;
	  }	  
	  
	  if (key == KeyEvent.VK_UP) {
	    upPressed = true;
	  }
	  
	  if (key == KeyEvent.VK_DOWN) {
	    downPressed = true;
	  }
	  
	  if (key == KeyEvent.VK_SPACE) {
	    shootPressed = true;		
	  }
	  
	  if (key == KeyEvent.VK_P || key == KeyEvent.VK_ESCAPE) {
		isPaused = !isPaused;
	  }	  
	  
	  if(key == KeyEvent.VK_F && evt.isAltDown()) {
	    stopGame();
	  }

      if (key == KeyEvent.VK_ENTER) {
	    if (gameOver){
		  init();
		} 
	  }	  
	}
	public void keyReleased(KeyEvent evt) {
	   int key = evt.getKeyCode();

      if (key == KeyEvent.VK_RIGHT) {
	    rightPressed = false;
	  } 	

      if (key == KeyEvent.VK_LEFT) {
	    leftPressed = false;
	  }
	  
	  if (key == KeyEvent.VK_UP) {
	    upPressed = false;
	  }
	  
	  if (key == KeyEvent.VK_DOWN) {
	    downPressed = false;
	  }
	  
	  if (key == KeyEvent.VK_SPACE) {
	    shootPressed = false;
		
	  }
	}
	public void keyTyped(KeyEvent evt) {}
	
	public void update() {
	  if (player.isDead){;
	    aliens.clear();
		bullets.clear();
		youLose = true;
		gameOver = true;
	  } else if(boss.isDead) {
	   processExplosions();
	    aliens.clear();
		bullets.clear();
		youWin = true;
		levelDone = true;
	  } else {
	
	  player.update(this);
	  for (Bullet bullet : bullets) {
	    bullet.update(this);
	  }
	  
	  for(Alien alien : aliens) {
	     alien.update(this);
	  }
	  
	  for(Bonus bonus : bonuses) {
	    bonus.update(this);
	  }
	  
	  if (remEnemies > 0) {
	    createAlien();
	  } else {
	    bossReached = true;
	  }
	  
	  if (bossReached && aliens.size() == 0){
	    bossReady = true;
	  } 
	  
	  createBonus();
	  processExplosions();
	  
	  if (bossReady) {
	    boss.update(this);
	  }
	  
	  if(gunState == 0) {
	    singleShot = true;
	    triShot = false;
	    fiveShot = false;
	  } else if(gunState == 1) {
	    singleShot = false;
	    triShot = true;
	    fiveShot = false;
	  } else if (gunState == 2) {
	    singleShot = false;
	   triShot = false;
	   fiveShot = true; 
	  }
	  
	  if (shootPressed) {
	    tryToShoot();
	  }
       backgroundY+=2;
		if(backgroundY >= 25){
			backgroundY = -background.getHeight(null)/4;
		}
	 }
	}
	
	public void render() {
	  player.draw(g);
	  for(Bullet bullet : bullets) {
	    bullet.draw(g); 
	  }
	  for(Alien alien : aliens) {
	     alien.draw(g);
	  }
	  
	  for(Explosion explosion : explosions){
	     explosion.drawChildren(this);
	  }
	  
	  for(Bonus bonus : bonuses) {
	    bonus.draw(g);
	  }
	  
	  if (bossReady){
	    boss.draw(g);
	  }
	}
	
	public void tryToShoot() {
	  if (System.currentTimeMillis() - lastShotTime < shootTime) {
	    return;
	  }else {
	  lastShotTime = System.currentTimeMillis();
       if(singleShot){
           bullets.add(new Bullet(player.getX()+player.getWidth()/2,player.getY(),10,0,"shot.gif")); 	   
	   }else if(triShot){
	     for(int i = 0; i < 3; i++){  
		   bullets.add(new Bullet(player.getX()+player.getWidth()/2,player.getY(),10, (i * 45)-45,"shot.gif"));
	     }
	   } else if(fiveShot) {
	     for(int i = 0; i < 5; i++){  
		   bullets.add(new Bullet(player.getX()+player.getWidth()/2,player.getY(),10, (i * 22.5)-45,"shot.gif"));
	     } 
	   }
	  }
	}
	
	public void createAlien() {
	  if (System.currentTimeMillis() - lastAlienTime < alienTime){
	     return;
	  }else {
	  lastAlienTime = System.currentTimeMillis();
	    for(int i = 0;i<level;i++) {
	      aliens.add(new Alien(gen.nextInt(WIDTH-70),0,Alien.NORMAL,"alien.gif"));
	    }
	  } 
	}
	
	public void createBonus() {
	  if (canCreateBonus) {
	    int bonusId = gen.nextInt(bonusTime);
		bonuses.add(new Bonus(bonusX,bonusY,bonusId,"ship.gif"));
		canCreateBonus = false;
	  }
	}
	
	public void processExplosions() {
	  if(explosionHappened) {
	    if(boss.isDead) {
		  for(int i = boss.getX();i<boss.getWidth();i++) {
		    explosions.add(new Explosion(5,i,(int)Math.random() * boss.getHeight() + boss.getY(),boss.getWidth()/2));
		  }
		  explosionHappened = false;
		}else{
	    explosions.add(new Explosion(5,explX,explY,30));
		explosionHappened = false;
		}
	  }
	  
	}
	
	public void levelDone() {
	    g.setFont(new Font("Serif",Font.BOLD,50));
	    g.setColor(Color.red);
	    g.drawString("Level Complete",(WIDTH/2) - 60,HEIGHT/2);
	    g.setFont(new Font("garamond",Font.PLAIN,40));
	    g.setColor(Color.blue);
	    g.drawString("Your Score: "+ score,400,450);
		
		level++;
		alienTime -= 100;
		boss.difficulty++;
		if(level%2 == 0) {
		  bonusTime += 3;
		}
		remEnemies = TARGET_AMOUNT + (level*5);
	    player.isDead = false;
	    boss.isDead = false;
	    isPaused = false;
	    bossReached = false;
	    bossReady = false;
		explosionHappened = false;
	    youWin = false;
	    youLose = false;
	    gameOver = false;
	    levelDone = false;
	    player.setHealth(100);
	    boss.setHealth(400);
	    player.setX(383);
	    player.setY(512);
	}
	
	public void gameOver() {
	  g = (Graphics2D)buffer.getDrawGraphics();
	 
	    g.setFont(new Font("Serif",Font.BOLD,60));
	    g.setColor(Color.red);
	    g.drawString("YOU ARE DEAD!!!",(WIDTH/2) - 60,HEIGHT/2);
	    g.setFont(new Font("garamond",Font.PLAIN,40));
	    g.setColor(Color.blue);
	    g.drawString("Your Score: "+ score,400,450);
		
	  g.dispose();
	  buffer.show();
	  
	  if(score > highScore){
	    highScore = score;
	  }
	}

    public static void main(String args[]){
	  new Space();	  
	}
}