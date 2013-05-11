class misterjones {
  static void main(String args[]) throws java.io.IOException { 
     new misterjones().run(new java.util.Scanner(System.in), 
                    new java.io.PrintWriter(System.out));
  }
  
  private static final int state$idle = 0;
  
  private static final int state$waitingForRest = 1;
  
  private static final int state$waitCandleFireBook = 2;
  
  private static final int state$waitFridgeFireBook = 3;
  
  private static final int state$waitFridgeCandleBook = 4;
  
  private static final int state$waitFireBook = 5;
  
  private static final int state$waitCandleBook = 6;
  
  private static final int state$waitFridgeBook = 7;
  
  private static final int state$waitFridgeFire = 8;
  
  private static final int state$waitCandleFire = 9;
  
  private static final int state$waitFridgeCandle = 10;
  
  private static final int state$waitBook = 11;
  
  private static final int state$waitFire = 12;
  
  private static final int state$waitCandle = 13;
  
  private static final int state$waitFridge = 14;
  
  private static final int state$active = 15;
  
  void run(java.util.Scanner input, java.io.Writer output) throws java.io.IOException {
    int state = state$idle;
    while (true) {
      String token = input.nextLine();
      switch (state) {
        
        case state$idle: {
          
             unlockGate(output);
          
             turnBackBookCase(output);
          
             turnBackCandleStick(output);
          
          
          if (gateClosed(token)) {
             state = state$waitingForRest;
          }
          
          if (gateOpened(token)) {
             state = state$idle;
          }
          
          if (candleStickTurnedBack(token)) {
             state = state$idle;
          }
          
          if (bookPlaced(token)) {
             state = state$idle;
          }
          
          if (fireOut(token)) {
             state = state$idle;
          }
          
          break;
        }
        
        case state$waitingForRest: {
          
          
          if (fridgeOpened(token)) {
             state = state$waitCandleFireBook;
          }
          
          if (candleStickTurned(token)) {
             state = state$waitFridgeFireBook;
          }
          
          if (fireplaceLit(token)) {
             state = state$waitFridgeCandleBook;
          }
          
          if (gateOpened(token)) {
             state = state$idle;
          }
          
          if (candleStickTurnedBack(token)) {
             state = state$idle;
          }
          
          if (bookPlaced(token)) {
             state = state$idle;
          }
          
          if (fireOut(token)) {
             state = state$idle;
          }
          
          break;
        }
        
        case state$waitCandleFireBook: {
          
          
          if (candleStickTurned(token)) {
             state = state$waitFireBook;
          }
          
          if (fireplaceLit(token)) {
             state = state$waitCandleBook;
          }
          
          if (bookTaken(token)) {
             state = state$waitCandleFire;
          }
          
          if (gateOpened(token)) {
             state = state$idle;
          }
          
          if (candleStickTurnedBack(token)) {
             state = state$idle;
          }
          
          if (bookPlaced(token)) {
             state = state$idle;
          }
          
          if (fireOut(token)) {
             state = state$idle;
          }
          
          break;
        }
        
        case state$waitFridgeFireBook: {
          
          
          if (fridgeOpened(token)) {
             state = state$waitFireBook;
          }
          
          if (fireplaceLit(token)) {
             state = state$waitFridgeBook;
          }
          
          if (bookTaken(token)) {
             state = state$waitFridgeFire;
          }
          
          if (gateOpened(token)) {
             state = state$idle;
          }
          
          if (candleStickTurnedBack(token)) {
             state = state$idle;
          }
          
          if (bookPlaced(token)) {
             state = state$idle;
          }
          
          if (fireOut(token)) {
             state = state$idle;
          }
          
          break;
        }
        
        case state$waitFridgeCandleBook: {
          
          
          if (candleStickTurned(token)) {
             state = state$waitFridgeBook;
          }
          
          if (bookTaken(token)) {
             state = state$waitFridgeCandle;
          }
          
          if (fridgeOpened(token)) {
             state = state$waitCandleBook;
          }
          
          if (gateOpened(token)) {
             state = state$idle;
          }
          
          if (candleStickTurnedBack(token)) {
             state = state$idle;
          }
          
          if (bookPlaced(token)) {
             state = state$idle;
          }
          
          if (fireOut(token)) {
             state = state$idle;
          }
          
          break;
        }
        
        case state$waitFireBook: {
          
          
          if (fireplaceLit(token)) {
             state = state$waitBook;
          }
          
          if (bookTaken(token)) {
             state = state$waitFire;
          }
          
          if (gateOpened(token)) {
             state = state$idle;
          }
          
          if (candleStickTurnedBack(token)) {
             state = state$idle;
          }
          
          if (bookPlaced(token)) {
             state = state$idle;
          }
          
          if (fireOut(token)) {
             state = state$idle;
          }
          
          break;
        }
        
        case state$waitCandleBook: {
          
          
          if (candleStickTurned(token)) {
             state = state$waitBook;
          }
          
          if (bookTaken(token)) {
             state = state$waitCandle;
          }
          
          if (gateOpened(token)) {
             state = state$idle;
          }
          
          if (candleStickTurnedBack(token)) {
             state = state$idle;
          }
          
          if (bookPlaced(token)) {
             state = state$idle;
          }
          
          if (fireOut(token)) {
             state = state$idle;
          }
          
          break;
        }
        
        case state$waitFridgeBook: {
          
          
          if (fridgeOpened(token)) {
             state = state$waitBook;
          }
          
          if (bookTaken(token)) {
             state = state$waitFridge;
          }
          
          if (gateOpened(token)) {
             state = state$idle;
          }
          
          if (candleStickTurnedBack(token)) {
             state = state$idle;
          }
          
          if (bookPlaced(token)) {
             state = state$idle;
          }
          
          if (fireOut(token)) {
             state = state$idle;
          }
          
          break;
        }
        
        case state$waitFridgeFire: {
          
          
          if (fireplaceLit(token)) {
             state = state$waitFridge;
          }
          
          if (fridgeOpened(token)) {
             state = state$waitFire;
          }
          
          if (gateOpened(token)) {
             state = state$idle;
          }
          
          if (candleStickTurnedBack(token)) {
             state = state$idle;
          }
          
          if (bookPlaced(token)) {
             state = state$idle;
          }
          
          if (fireOut(token)) {
             state = state$idle;
          }
          
          break;
        }
        
        case state$waitCandleFire: {
          
          
          if (candleStickTurned(token)) {
             state = state$waitFire;
          }
          
          if (fireplaceLit(token)) {
             state = state$waitCandle;
          }
          
          if (gateOpened(token)) {
             state = state$idle;
          }
          
          if (candleStickTurnedBack(token)) {
             state = state$idle;
          }
          
          if (bookPlaced(token)) {
             state = state$idle;
          }
          
          if (fireOut(token)) {
             state = state$idle;
          }
          
          break;
        }
        
        case state$waitFridgeCandle: {
          
          
          if (fridgeOpened(token)) {
             state = state$waitCandle;
          }
          
          if (candleStickTurned(token)) {
             state = state$waitFridge;
          }
          
          if (gateOpened(token)) {
             state = state$idle;
          }
          
          if (candleStickTurnedBack(token)) {
             state = state$idle;
          }
          
          if (bookPlaced(token)) {
             state = state$idle;
          }
          
          if (fireOut(token)) {
             state = state$idle;
          }
          
          break;
        }
        
        case state$waitBook: {
          
             lockGate(output);
          
             activateTurrets(output);
          
          
          if (bookTaken(token)) {
             state = state$active;
          }
          
          if (gateOpened(token)) {
             state = state$idle;
          }
          
          if (candleStickTurnedBack(token)) {
             state = state$idle;
          }
          
          if (bookPlaced(token)) {
             state = state$idle;
          }
          
          if (fireOut(token)) {
             state = state$idle;
          }
          
          break;
        }
        
        case state$waitFire: {
          
             lockGate(output);
          
             activateTurrets(output);
          
          
          if (fireplaceLit(token)) {
             state = state$active;
          }
          
          if (gateOpened(token)) {
             state = state$idle;
          }
          
          if (candleStickTurnedBack(token)) {
             state = state$idle;
          }
          
          if (bookPlaced(token)) {
             state = state$idle;
          }
          
          if (fireOut(token)) {
             state = state$idle;
          }
          
          break;
        }
        
        case state$waitCandle: {
          
             lockGate(output);
          
             activateTurrets(output);
          
          
          if (candleStickTurned(token)) {
             state = state$active;
          }
          
          if (gateOpened(token)) {
             state = state$idle;
          }
          
          if (candleStickTurnedBack(token)) {
             state = state$idle;
          }
          
          if (bookPlaced(token)) {
             state = state$idle;
          }
          
          if (fireOut(token)) {
             state = state$idle;
          }
          
          break;
        }
        
        case state$waitFridge: {
          
             lockGate(output);
          
             activateTurrets(output);
          
          
          if (fridgeOpened(token)) {
             state = state$active;
          }
          
          if (gateOpened(token)) {
             state = state$idle;
          }
          
          if (candleStickTurnedBack(token)) {
             state = state$idle;
          }
          
          if (bookPlaced(token)) {
             state = state$idle;
          }
          
          if (fireOut(token)) {
             state = state$idle;
          }
          
          break;
        }
        
        case state$active: {
          
             turnBookCase(output);
          
          
          if (bookTaken(token)) {
             state = state$active;
          }
          
          if (gateOpened(token)) {
             state = state$idle;
          }
          
          if (candleStickTurnedBack(token)) {
             state = state$idle;
          }
          
          if (bookPlaced(token)) {
             state = state$idle;
          }
          
          if (fireOut(token)) {
             state = state$idle;
          }
          
          break;
        }
        
      }
    }
  }
  
  private boolean fridgeOpened(String token) {
    return token.equals("F1OP");
  }
  
  private boolean candleStickTurned(String token) {
    return token.equals("CSTR");
  }
  
  private boolean candleStickTurnedBack(String token) {
    return token.equals("CSTB");
  }
  
  private boolean fireplaceLit(String token) {
    return token.equals("FPLT");
  }
  
  private boolean fireOut(String token) {
    return token.equals("FPOT");
  }
  
  private boolean bookTaken(String token) {
    return token.equals("BKTK");
  }
  
  private boolean bookPlaced(String token) {
    return token.equals("BKPL");
  }
  
  private boolean gateOpened(String token) {
    return token.equals("GTOP");
  }
  
  private boolean gateClosed(String token) {
    return token.equals("GTCL");
  }
  
  
  private void activateTurrets(java.io.Writer output) throws java.io.IOException {
    output.write("ACTU\n");
    output.flush();
  }
  
  private void lockGate(java.io.Writer output) throws java.io.IOException {
    output.write("GTLK\n");
    output.flush();
  }
  
  private void unlockGate(java.io.Writer output) throws java.io.IOException {
    output.write("GTUK\n");
    output.flush();
  }
  
  private void turnBookCase(java.io.Writer output) throws java.io.IOException {
    output.write("TRBC\n");
    output.flush();
  }
  
  private void turnBackBookCase(java.io.Writer output) throws java.io.IOException {
    output.write("TBBC\n");
    output.flush();
  }
  
  private void turnBackCandleStick(java.io.Writer output) throws java.io.IOException {
    output.write("TBCS\n");
    output.flush();
  }
  
}