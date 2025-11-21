import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.text.DecimalFormat;
import java.util.*;
import java.util.List;
import java.util.regex.*;
import javax.swing.*;
import javax.swing.border.*;

public class Calculator extends JFrame {

    // --- UI font: try Inter, fallback to SansSerif ---
    private static final Font UI_FONT = findUiFont();

    private final DisplayField display;
    private boolean degreeMode = true;
    private final DecimalFormat shortFmt = new DecimalFormat("0.##########");
    private final JPanel mainGlass;

    public Calculator() {
        super("Calculator");

        // --- Global AA / font smoothing (early) ---
        System.setProperty("awt.useSystemAAFontSettings", "on");
        System.setProperty("swing.aatext", "true");

        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(420, 620));
        setLayout(new BorderLayout());

        // Overall background panel (dark)
        JPanel background = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                applyQualityHints(g2);
                Color c1 = new Color(24, 28, 34);
                Color c2 = new Color(14, 18, 24);
                GradientPaint gp = new GradientPaint(0, 0, c1, 0, getHeight(), c2);
                g2.setPaint(gp);
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.dispose();
            }
        };
        add(background, BorderLayout.CENTER);

        // Glass panel (center container)
        mainGlass = new GlassPanel();
        mainGlass.setLayout(new GridBagLayout());
        mainGlass.setOpaque(false);
        mainGlass.setBorder(new EmptyBorder(18, 18, 18, 18));

        background.add(mainGlass, BorderLayout.CENTER);

        // Top area: display and mode toggle
        display = new DisplayField();
        display.setPreferredSize(new Dimension(360, 80));

        // Controls panel (mode toggle and small status)
        JPanel topRow = new JPanel(new BorderLayout());
        topRow.setOpaque(false);
        topRow.add(display, BorderLayout.CENTER);

        // Degree / Radian toggle
        JToggleButton degBtn = createToggle("DEG", true);
        JToggleButton radBtn = createToggle("RAD", false);

        ButtonGroup grp = new ButtonGroup();
        grp.add(degBtn);
        grp.add(radBtn);
        degBtn.setSelected(true);

        JPanel modePanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 6));
        modePanel.setOpaque(false);
        modePanel.add(degBtn);
        modePanel.add(radBtn);
        topRow.add(modePanel, BorderLayout.SOUTH);

        // Layout constraints
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0; c.gridy = 0; c.weightx = 1; c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(6, 6, 12, 6);
        mainGlass.add(topRow, c);

        // Buttons area
        JPanel buttons = createButtonGrid();
        c = new GridBagConstraints();
        c.gridx = 0; c.gridy = 1; c.weightx = 1; c.weighty = 1;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new Insets(6, 6, 6, 6);
        mainGlass.add(buttons, c);

        // Footer info
        JLabel hint = new JLabel("Material • Glass • Icons • Parser • Keyboard: numbers/operators/Enter/Esc/Backspace");
        hint.setForeground(new Color(180, 190, 200, 200));
        hint.setFont(UI_FONT.deriveFont(Font.PLAIN, 11f));
        c = new GridBagConstraints();
        c.gridx = 0; c.gridy = 2; c.weightx = 1; c.anchor = GridBagConstraints.SOUTHWEST;
        c.insets = new Insets(6, 6, 2, 6);
        mainGlass.add(hint, c);

        // Degree/radian action
        degBtn.addActionListener(e -> degreeMode = true);
        radBtn.addActionListener(e -> degreeMode = false);

        // Key bindings on root pane
        setupKeyBindings();

        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }

    // Choose a readable UI font (attempt to use Inter, fallback)
    private static Font findUiFont() {
        String preferred = "Inter";
        Font f = new Font(preferred, Font.PLAIN, 14);
        if (!isFontAvailable(preferred)) {
            return new Font("SansSerif", Font.PLAIN, 14);
        }
        return f;
    }
    private static boolean isFontAvailable(String name) {
        String[] fonts = GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames();
        for (String s : fonts) if (s.equalsIgnoreCase(name)) return true;
        return false;
    }

    // Display field with custom look
    private class DisplayField extends JPanel {
        private String text = "";
        DisplayField() {
            setOpaque(false);
            setPreferredSize(new Dimension(360, 80));
            setBorder(new EmptyBorder(10, 16, 10, 16));
        }

        void setText(String s) { text = s == null ? "" : s; repaint(); }
        String getTextValue() { return text; }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            applyQualityHints(g2);

            int w = getWidth(), h = getHeight();

            // translucent rounded background
            RoundRectangle2D bg = new RoundRectangle2D.Float(0, 0, w, h, 18, 18);
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.18f));
            g2.setPaint(new Color(255, 255, 255));
            g2.fill(bg);

            // inner slightly more opaque band to simulate frosted effect
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.06f));
            g2.setPaint(new GradientPaint(0, 0, new Color(255,255,255), 0, h, new Color(255,255,255,0)));
            g2.fill(bg);

            // border
            g2.setComposite(AlphaComposite.SrcOver);
            g2.setStroke(new BasicStroke(1f));
            g2.setPaint(new Color(255, 255, 255, 40));
            g2.draw(bg);

            // text (right aligned)
            g2.setPaint(Color.WHITE);
            g2.setFont(UI_FONT.deriveFont(Font.BOLD, Math.max(20, h/3f)));
            FontMetrics fm = g2.getFontMetrics();
            String disp = text.isEmpty() ? "0" : text;
            int textWidth = fm.stringWidth(disp);
            int x = Math.max(12, w - textWidth - 20);
            int y = (h + fm.getAscent())/2 - 6;
            g2.drawString(disp, x, y);
            g2.dispose();
        }
    }

    // Small reusable toggle button style
    private JToggleButton createToggle(String text, boolean defaultState) {
        JToggleButton b = new JToggleButton(text);
        b.setForeground(Color.WHITE);
        b.setBackground(new Color(255,255,255,8));
        b.setFocusPainted(false);
        b.setBorder(new RoundedBorder(10, new Color(255,255,255,40)));
        b.setOpaque(false);
        b.setFont(UI_FONT.deriveFont(Font.BOLD, 11f));
        b.setPreferredSize(new Dimension(56, 28));
        b.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                b.setBackground(new Color(255,255,255,14));
                b.setBorder(new RoundedBorder(10, new Color(255,255,255,100)));
            } else {
                b.setBackground(new Color(255,255,255,8));
                b.setBorder(new RoundedBorder(10, new Color(255,255,255,40)));
            }
            b.repaint();
        });
        return b;
    }

    // Build the buttons grid (responsive)
    private JPanel createButtonGrid() {
        GridBagConstraints c;
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setOpaque(false);

        String[] labels = {
            "(", ")", "mod", "ANS", "C",
            "sin", "cos", "tan", "log", "ln",
            "√", "x²", "x^y", "1/x", "%",
            "7", "8", "9", "÷", "π",
            "4", "5", "6", "×", "e",
            "1", "2", "3", "-", "±",
            "0", ".", "ANS->", "+", "="
        };

        int cols = 5;
        int rows = (labels.length + cols - 1) / cols;
        int idx = 0;
        for (int r = 0; r < rows; r++) {
            for (int col = 0; col < cols; col++) {
                if (idx >= labels.length) break;
                String lbl = labels[idx++];
                CalcButton btn = new CalcButton(lbl);
                btn.addActionListener(ev -> handleButton(lbl));
                c = new GridBagConstraints();
                c.gridx = col; c.gridy = r; c.weightx = 1.0; c.weighty = 1.0;
                c.fill = GridBagConstraints.BOTH;
                c.insets = new Insets(6, 6, 6, 6);
                panel.add(btn, c);
            }
        }

        return panel;
    }

    // Custom material-style button
    private class CalcButton extends JButton {
        CalcButton(String text) {
            super(text);
            setFocusPainted(false);
            setContentAreaFilled(false);
            setOpaque(false);
            setForeground(Color.WHITE);
            setFont(UI_FONT.deriveFont(Font.BOLD, 14f));
            setBorder(new EmptyBorder(10, 6, 10, 6));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            // hover effect
            addMouseListener(new MouseAdapter() {
                @Override public void mouseEntered(MouseEvent e) { repaint(); }
                @Override public void mouseExited(MouseEvent e) { repaint(); }
            });
        }

        @Override
        protected void paintComponent(Graphics gg) {
            // We intentionally do NOT call super.paintComponent to avoid default LAF drawing (which can interfere with custom rendering)
            Graphics2D g = (Graphics2D) gg.create();
            applyQualityHints(g);

            int w = getWidth(), h = getHeight();
            // rounded rect base
            RoundRectangle2D rr = new RoundRectangle2D.Float(0, 0, Math.max(0, w-1), Math.max(0, h-1), 12, 12);

            // button fill: subtle translucent material color
            Color base = new Color(255,255,255,6);
            g.setComposite(AlphaComposite.SrcOver);
            g.setPaint(base);
            g.fill(rr);

            // accent for operators
            String t = getText();
            boolean isOp = t.matches("÷|×|\\-|\\+|%=|=|C|mod|x\\^y|1/x|%");
            if (isOp) {
                g.setPaint(new Color(120, 110, 255, 40));
                g.fill(rr);
            }

            // hover overlay
            if (getModel().isRollover() || getModel().isPressed()) {
                g.setPaint(new Color(255,255,255,10));
                g.fill(rr);
            }

            // border
            g.setStroke(new BasicStroke(1f));
            g.setPaint(new Color(255,255,255,24));
            g.draw(rr);

            // draw label (centered)
            g.setPaint(Color.WHITE);
            Font f = UI_FONT.deriveFont(Font.BOLD, ("÷".equals(getText()) || "×".equals(getText()) || "+".equals(getText()) || "-".equals(getText()) || "=".equals(getText())) ? 18f : 14f);
            g.setFont(f);
            FontMetrics fm = g.getFontMetrics();
            String txt = getText();
            int sw = fm.stringWidth(txt);
            int sx = (w - sw) / 2;
            int sy = (h + fm.getAscent()) / 2 - 4;
            // vertical adjustment for single-char operators
            if (txt.length() == 1) sy += 0;
            g.drawString(txt, sx, sy);

            g.dispose();
        }

        @Override
        public Dimension getPreferredSize() {
            // make buttons reasonably square-ish
            Dimension d = super.getPreferredSize();
            int size = Math.max(48, Math.min(120, d.height + 8));
            return new Dimension(d.width + 10, size);
        }
    }

    // Rounded border helper
    private static class RoundedBorder implements Border {
        private final int radius;
        private final Color color;
        RoundedBorder(int r, Color c) { radius = r; color = c; }
        public Insets getBorderInsets(Component c) { return new Insets(radius, radius, radius, radius); }
        public boolean isBorderOpaque() { return false; }
        public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
            Graphics2D g2 = (Graphics2D) g.create();
            applyQualityHints(g2);
            g2.setPaint(color);
            g2.setStroke(new BasicStroke(1.4f));
            g2.drawRoundRect(x+1, y+1, width-3, height-3, radius, radius);
            g2.dispose();
        }
    }

    // Glass panel with subtle layered look
    private class GlassPanel extends JPanel {
        GlassPanel() {
            setOpaque(false);
            setBorder(new EmptyBorder(12, 12, 12, 12));
        }
        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            applyQualityHints(g2);
            int w = getWidth(), h = getHeight();

            // outer shadow (soft)
            float radius = Math.max(w, h);
            try {
                g2.setPaint(new RadialGradientPaint(new Point(w/2, -h/2), radius,
                    new float[]{0f, 1f},
                    new Color[]{new Color(0,0,0,60), new Color(0,0,0,0)}));
                g2.fillRect(0, 0, w, h);
            } catch (Exception ex) {
                // ignore on older JVMs
            }

            // frosted glass rectangle
            RoundRectangle2D rr = new RoundRectangle2D.Float(0, 0, Math.max(0, w-1), Math.max(0, h-1), 28, 28);
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.06f));
            g2.setPaint(Color.WHITE);
            g2.fill(rr);

            // gradient highlight
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.04f));
            g2.setPaint(new GradientPaint(0, 0, Color.WHITE, 0, h, new Color(255, 255, 255, 0)));
            g2.fill(rr);

            // border
            g2.setComposite(AlphaComposite.SrcOver);
            g2.setPaint(new Color(255, 255, 255, 35));
            g2.setStroke(new BasicStroke(1.2f));
            g2.draw(rr);

            g2.dispose();
            super.paintComponent(g);
        }
    }

    // Handle button labels -> actions
    private void handleButton(String label) {
        try {
            switch (label) {
                case "C": display.setText(""); return;
                case "ANS":
                case "ANS->":
                    // placeholder for answer storage — not implemented
                    return;
                case "±":
                    toggleSign();
                    return;
                case "π":
                    appendText(shortFmt.format(Math.PI));
                    return;
                case "e":
                    appendText(shortFmt.format(Math.E));
                    return;
                case "x²":
                    appendText("^2");
                    return;
                case "x^y":
                    appendText("^");
                    return;
                case "mod":
                    appendText(" mod ");
                    return;
                case "1/x":
                    appendText("(1/");
                    return;
                case "√":
                    appendText("sqrt(");
                    return;
                case "sin": case "cos": case "tan": case "log": case "ln":
                    appendText(label + "(");
                    return;
                case "%":
                    appendText("%");
                    return;
                case "÷":
                    appendText("/");
                    return;
                case "×":
                    appendText("*");
                    return;
                case "(":
                    appendText("(");
                    return;
                case ")":
                    appendText(")");
                    return;
                case "=":
                    evaluateAndShow();
                    return;
                default:
                    // number or dot or plus/minus or other
                    appendText(label);
            }
        } catch (Exception ex) {
            display.setText("Error");
        }
    }

    private void appendText(String s) {
        String now = display.getTextValue();
        if (now.equals("0") || now.isEmpty()) now = "";
        now += s;
        display.setText(now);
    }

    private void toggleSign() {
        String cur = display.getTextValue();
        if (cur.isEmpty() || cur.equals("0")) return;
        if (cur.startsWith("-")) cur = cur.substring(1);
        else cur = "-" + cur;
        display.setText(cur);
    }

    /* ========== Expression parser (Shunting-yard) + evaluator ========== */
    // (left intact - UI-only change requested)

    private void evaluateAndShow() {
        String expr = display.getTextValue();
        if (expr == null || expr.trim().isEmpty()) return;
        try {
            double val = evaluateExpression(expr);
            display.setText(shortFmt.format(val));
        } catch (Exception ex) {
            display.setText("Error");
        }
    }

    private double evaluateExpression(String expr) throws Exception {
        List<Token> tokens = tokenize(expr);
        List<Token> rpn = shuntingYard(tokens);
        return evaluateRPN(rpn);
    }

    // Tokenization (left as originally provided)
    private static final Pattern TOKEN_PAT =
            Pattern.compile("\\s*(?:([0-9]*\\.?[0-9]+)|([a-zA-Z_][a-zA-Z0-9_]*)|([()+\\-*/^%])|(.) )");

    private List<Token> tokenize(String s) throws Exception {
        List<Token> out = new ArrayList<>();
        // Simplified tolerant tokenization using iterative scan
        Matcher m = Pattern.compile("\\s*([0-9]*\\.?[0-9]+|[a-zA-Z_][a-zA-Z0-9_]*|[()+\\-*/^%]|\\S)").matcher(s);
        int pos = 0;
        while (m.find()) {
            pos = m.end();
            String tok = m.group(1);
            if (tok == null) continue;
            if (tok.matches("[0-9]*\\.?[0-9]+")) {
                out.add(new Token(TokenType.NUMBER, tok));
            } else if (tok.matches("[a-zA-Z_][a-zA-Z0-9_]*")) {
                String id = tok;
                if (id.equalsIgnoreCase("pi") || id.equals("π")) {
                    out.add(new Token(TokenType.NUMBER, String.valueOf(Math.PI)));
                } else if (id.equalsIgnoreCase("e")) {
                    out.add(new Token(TokenType.NUMBER, String.valueOf(Math.E)));
                } else if (id.equalsIgnoreCase("mod")) {
                    out.add(new Token(TokenType.OPERATOR, "mod"));
                } else {
                    out.add(new Token(TokenType.FUNCTION_OR_VAR, id));
                }
            } else if (tok.length() == 1 && "()+-*/^%".indexOf(tok.charAt(0)) >= 0) {
                String sym = tok;
                if (sym.equals("+") || sym.equals("-") || sym.equals("*") || sym.equals("/") || sym.equals("^") || sym.equals("%")) {
                    out.add(new Token(TokenType.OPERATOR, sym));
                } else if (sym.equals("(")) out.add(new Token(TokenType.LEFT_PAREN, sym));
                else if (sym.equals(")")) out.add(new Token(TokenType.RIGHT_PAREN, sym));
                else throw new Exception("Unknown symbol: " + sym);
            } else {
                // catch-all (single non-space char)
                throw new Exception("Unexpected token near: " + s.substring(Math.max(0,pos-10), Math.min(s.length(), pos+10)));
            }
        }

        // post-process: detect implicit multiplication like ")(" or "2(" or ")2" or "pi(" etc.
        List<Token> processed = new ArrayList<>();
        Token prev = null;
        for (Token t : out) {
            if (prev != null) {
                if ((prev.isNumber() || prev.isRightParen() || prev.isVar()) && (t.isLeftParen() || t.isFuncOrVar())) {
                    processed.add(new Token(TokenType.OPERATOR, "*"));
                }
            }
            processed.add(t);
            prev = t;
        }
        return processed;
    }

    // Shunting-yard to produce RPN
    private List<Token> shuntingYard(List<Token> tokens) throws Exception {
        List<Token> output = new ArrayList<>();
        Deque<Token> stack = new ArrayDeque<>();

        for (int i = 0; i < tokens.size(); i++) {
            Token t = tokens.get(i);
            if (t.type == TokenType.NUMBER) {
                output.add(t);
            } else if (t.type == TokenType.FUNCTION_OR_VAR) {
                // could be function if next token is '('
                Token next = (i + 1 < tokens.size()) ? tokens.get(i + 1) : null;
                if (next != null && next.type == TokenType.LEFT_PAREN) {
                    stack.push(t); // function
                } else {
                    // variable (unsupported except constants handled earlier)
                    output.add(t);
                }
            } else if (t.type == TokenType.OPERATOR) {
                String op1 = t.value;
                while (!stack.isEmpty() && stack.peek().type == TokenType.OPERATOR) {
                    String op2 = stack.peek().value;
                    if ((isLeftAssoc(op1) && precedence(op1) <= precedence(op2)) ||
                        (!isLeftAssoc(op1) && precedence(op1) < precedence(op2))) {
                        output.add(stack.pop());
                    } else break;
                }
                stack.push(t);
            } else if (t.type == TokenType.LEFT_PAREN) {
                stack.push(t);
            } else if (t.type == TokenType.RIGHT_PAREN) {
                while (!stack.isEmpty() && stack.peek().type != TokenType.LEFT_PAREN) {
                    output.add(stack.pop());
                }
                if (stack.isEmpty()) throw new Exception("Mismatched parentheses");
                stack.pop(); // pop left paren
                if (!stack.isEmpty() && stack.peek().type == TokenType.FUNCTION_OR_VAR) {
                    output.add(stack.pop()); // function call
                }
            }
        }

        while (!stack.isEmpty()) {
            Token tk = stack.pop();
            if (tk.type == TokenType.LEFT_PAREN || tk.type == TokenType.RIGHT_PAREN) throw new Exception("Mismatched parentheses");
            output.add(tk);
        }
        return output;
    }

    // Evaluate RPN
    private double evaluateRPN(List<Token> rpn) throws Exception {
        Deque<Double> st = new ArrayDeque<>();
        for (Token t : rpn) {
            if (t.type == TokenType.NUMBER) {
                st.push(Double.parseDouble(t.value));
            } else if (t.type == TokenType.OPERATOR) {
                String op = t.value;
                if (op.equals("mod")) op = "%";
                if (op.equals("%")) {
                    if (st.isEmpty()) throw new Exception("Missing operand for %");
                    double a = st.pop();
                    st.push(a / 100.0);
                    continue;
                }
                if (isBinaryOperator(op)) {
                    if (st.size() < 2) throw new Exception("Not enough operands for " + op);
                    double b = st.pop();
                    double a = st.pop();
                    st.push(applyBinary(op, a, b));
                } else throw new Exception("Unknown operator: " + op);
            } else if (t.type == TokenType.FUNCTION_OR_VAR) {
                String fn = t.value.toLowerCase();
                if (st.isEmpty()) throw new Exception("Missing argument for function " + fn);
                double a = st.pop();
                st.push(applyFunction(fn, a));
            } else {
                throw new Exception("Unexpected RPN token: " + t);
            }
        }
        if (st.size() != 1) throw new Exception("Parse error (stack size != 1)");
        return st.pop();
    }

    private boolean isBinaryOperator(String op) {
        return "+".equals(op) || "-".equals(op) || "*".equals(op) || "/".equals(op) || "^".equals(op) || "%".equals(op) || "mod".equals(op);
    }

    private double applyBinary(String op, double a, double b) throws Exception {
        switch (op) {
            case "+": return a + b;
            case "-": return a - b;
            case "*": return a * b;
            case "/":
                if (b == 0) throw new Exception("Division by zero");
                return a / b;
            case "^": return Math.pow(a, b);
            case "%": return a % b;
            default: throw new Exception("Unknown binary op " + op);
        }
    }

    private double applyFunction(String fn, double a) throws Exception {
        switch (fn) {
            case "sin": return Math.sin(toRadiansIfNeeded(a));
            case "cos": return Math.cos(toRadiansIfNeeded(a));
            case "tan": return Math.tan(toRadiansIfNeeded(a));
            case "asin": return fromRadiansIfNeeded(Math.asin(a));
            case "acos": return fromRadiansIfNeeded(Math.acos(a));
            case "atan": return fromRadiansIfNeeded(Math.atan(a));
            case "sqrt": return Math.sqrt(a);
            case "ln": return Math.log(a);
            case "log": return Math.log10(a);
            case "abs": return Math.abs(a);
            case "neg": return -a;
            default: throw new Exception("Unknown function: " + fn);
        }
    }

    private double toRadiansIfNeeded(double x) {
        return degreeMode ? Math.toRadians(x) : x;
    }
    private double fromRadiansIfNeeded(double x) {
        return degreeMode ? Math.toDegrees(x) : x;
    }

    private int precedence(String op) {
        switch (op) {
            case "+": case "-": return 2;
            case "*": case "/": case "%": case "mod": return 3;
            case "^": return 4;
        }
        return 0;
    }

    private boolean isLeftAssoc(String op) {
        // ^ is right-assoc
        return !("^".equals(op));
    }

    // Token classes
    enum TokenType { NUMBER, OPERATOR, LEFT_PAREN, RIGHT_PAREN, FUNCTION_OR_VAR }
    static class Token {
        final TokenType type;
        final String value;
        Token(TokenType t, String v) { type = t; value = v; }
        boolean isNumber() { return type == TokenType.NUMBER; }
        boolean isLeftParen() { return type == TokenType.LEFT_PAREN; }
        boolean isRightParen() { return type == TokenType.RIGHT_PAREN; }
        boolean isVar() { return type == TokenType.FUNCTION_OR_VAR; }
        boolean isFuncOrVar() { return type == TokenType.FUNCTION_OR_VAR; }
        @Override public String toString() { return type + ":" + value; }
    }

    // Key bindings
    private void setupKeyBindings() {
        JRootPane root = getRootPane();

        InputMap im = root.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap am = root.getActionMap();

        // digits and dot
        for (int i = 0; i <= 9; i++) {
            String k = String.valueOf(i);
            String actionKey = "digit" + k;
            im.put(KeyStroke.getKeyStroke("typed " + k), actionKey);
            final String digit = k;
            am.put(actionKey, new AbstractAction() { public void actionPerformed(ActionEvent e) { appendText(digit); }});
        }
        im.put(KeyStroke.getKeyStroke("typed ."), "dot");
        am.put("dot", new AbstractAction() { public void actionPerformed(ActionEvent e) { appendText("."); }});

        // operators (typed)
        im.put(KeyStroke.getKeyStroke("typed +"), "+");
        am.put("+", new AbstractAction() { public void actionPerformed(ActionEvent e) { appendText("+"); }});
        im.put(KeyStroke.getKeyStroke("typed -"), "-");
        am.put("-", new AbstractAction() { public void actionPerformed(ActionEvent e) { appendText("-"); }});
        im.put(KeyStroke.getKeyStroke("typed *"), "*");
        am.put("*", new AbstractAction() { public void actionPerformed(ActionEvent e) { appendText("*"); }});
        im.put(KeyStroke.getKeyStroke("typed /"), "/");
        am.put("/", new AbstractAction() { public void actionPerformed(ActionEvent e) { appendText("/"); }});
        im.put(KeyStroke.getKeyStroke("typed ("), "(");
        am.put("(", new AbstractAction() { public void actionPerformed(ActionEvent e) { appendText("("); }});
        im.put(KeyStroke.getKeyStroke("typed )"), ")");
        am.put(")", new AbstractAction() { public void actionPerformed(ActionEvent e) { appendText(")"); }});

        // Enter / backspace / escape
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "equals");
        am.put("equals", new AbstractAction() { public void actionPerformed(ActionEvent e) { evaluateAndShow(); }});
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_BACK_SPACE, 0), "back");
        am.put("back", new AbstractAction() { public void actionPerformed(ActionEvent e) {
            String cur = display.getTextValue();
            if (!cur.isEmpty()) display.setText(cur.substring(0, Math.max(0, cur.length()-1)));
        }});
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "clear");
        am.put("clear", new AbstractAction() { public void actionPerformed(ActionEvent e) { display.setText(""); }});

        // shortcuts for functions (single typed letters)
        im.put(KeyStroke.getKeyStroke("typed s"), "sin(");
        am.put("sin(", new AbstractAction() { public void actionPerformed(ActionEvent e) { appendText("sin("); }});
        im.put(KeyStroke.getKeyStroke("typed c"), "cos(");
        am.put("cos(", new AbstractAction() { public void actionPerformed(ActionEvent e) { appendText("cos("); }});
        im.put(KeyStroke.getKeyStroke("typed t"), "tan(");
        am.put("tan(", new AbstractAction() { public void actionPerformed(ActionEvent e) { appendText("tan("); }});
        im.put(KeyStroke.getKeyStroke("typed l"), "ln(");
        am.put("ln(", new AbstractAction() { public void actionPerformed(ActionEvent e) { appendText("ln("); }});

        // degree/radian toggles by key (d/r)
        im.put(KeyStroke.getKeyStroke("typed d"), "deg");
        am.put("deg", new AbstractAction() { public void actionPerformed(ActionEvent e) { degreeMode = true; }});
        im.put(KeyStroke.getKeyStroke("typed r"), "rad");
        am.put("rad", new AbstractAction() { public void actionPerformed(ActionEvent e) { degreeMode = false; }});
    }

    // small helper (left for clarity)
    private void mapKeyTyped(InputMap im, String key, String actionKey) {
        im.put(KeyStroke.getKeyStroke("typed " + key.trim()), actionKey);
    }

    // ---------- Entry ----------
    public static void main(String[] args) {
        SwingUtilities.invokeLater(Calculator::new);
    }

    // --- Utility helpers ---
    private static void applyQualityHints(Graphics2D g2) {
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
    }
}
