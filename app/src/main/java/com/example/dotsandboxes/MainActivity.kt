package com.example.dotsandboxes

import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.google.android.material.textfield.TextInputEditText

class MainActivity : AppCompatActivity() {

    private lateinit var setupScreen: FrameLayout
    private lateinit var gameContainer: FrameLayout
    private lateinit var titleText: TextView
    private lateinit var subtitleText: TextView
    private lateinit var player1Card: CardView
    private lateinit var player2Card: CardView
    private lateinit var p1Name: EditText
    private lateinit var p2Name: EditText
    private lateinit var p1Color: TextInputEditText
    private lateinit var p2Color: TextInputEditText
    private lateinit var btnSize4: Button
    private lateinit var btnSize6: Button
    private lateinit var btnSize8: Button
    private lateinit var startBtn: Button
    private lateinit var scoreBoard: LinearLayout
    private lateinit var cardP1: CardView
    private lateinit var cardP2: CardView
    private lateinit var scoreP1: TextView
    private lateinit var scoreP2: TextView
    private lateinit var turnIndicator: CardView
    private lateinit var turnText: TextView
    private lateinit var boardContainer: FrameLayout
    private lateinit var board: FrameLayout
    private lateinit var previewLine: View

    private var gridSize = 6
    private var spacing = 50
    private var currentPlayer = "p1"
    private val players = mutableMapOf(
        "p1" to Player("أحمد", "#3498db", 0),
        "p2" to Player("سارة", "#e74c3c", 0)
    )
    private val drawnLines = mutableSetOf<String>()
    private var startDot: Dot? = null

    data class Player(val name: String, val color: String, var score: Int)
    data class Dot(val row: Int, val col: Int, val x: Float, val y: Float)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initViews()
        setupListeners()
        updateGridButtons()
    }

    private fun initViews() {
        setupScreen = findViewById(R.id.setupScreen)
        gameContainer = findViewById(R.id.gameContainer)
        titleText = findViewById(R.id.titleText)
        subtitleText = findViewById(R.id.subtitleText)
        player1Card = findViewById(R.id.player1Card)
        player2Card = findViewById(R.id.player2Card)
        p1Name = findViewById(R.id.p1Name)
        p2Name = findViewById(R.id.p2Name)
        p1Color = findViewById(R.id.p1Color)
        p2Color = findViewById(R.id.p2Color)
        btnSize4 = findViewById(R.id.btnSize4)
        btnSize6 = findViewById(R.id.btnSize6)
        btnSize8 = findViewById(R.id.btnSize8)
        startBtn = findViewById(R.id.startBtn)
        scoreBoard = findViewById(R.id.scoreBoard)
        cardP1 = findViewById(R.id.cardP1)
        cardP2 = findViewById(R.id.cardP2)
        scoreP1 = findViewById(R.id.scoreP1)
        scoreP2 = findViewById(R.id.scoreP2)
        turnIndicator = findViewById(R.id.turnIndicator)
        turnText = findViewById(R.id.turnText)
        boardContainer = findViewById(R.id.boardContainer)
        board = findViewById(R.id.board)
        previewLine = findViewById(R.id.previewLine)
    }

    private fun setupListeners() {
        p1Name.setText(players["p1"]?.name)
        p2Name.setText(players["p2"]?.name)
        p1Color.setText(players["p1"]?.color)
        p2Color.setText(players["p2"]?.color)

        btnSize4.setOnClickListener { selectGridSize(4) }
        btnSize6.setOnClickListener { selectGridSize(6) }
        btnSize8.setOnClickListener { selectGridSize(8) }
        startBtn.setOnClickListener { startGame() }
    }

    private fun selectGridSize(size: Int) {
        gridSize = size
        updateGridButtons()
    }

    private fun updateGridButtons() {
        val buttons = listOf(btnSize4, btnSize6, btnSize8)
        buttons.forEach { btn ->
            btn.setBackgroundResource(if (btn.text.toString() == "${gridSize}x${gridSize}") R.drawable.btn_option_selected_background else R.drawable.btn_option_background)
        }
    }

    private fun startGame() {
        players["p1"] = Player(p1Name.text.toString(), p1Color.text.toString(), 0)
        players["p2"] = Player(p2Name.text.toString(), p2Color.text.toString(), 0)
        
        setupScreen.visibility = View.GONE
        gameContainer.visibility = View.VISIBLE
        
        initBoard()
        updateScores()
        updateTurnIndicator()
    }

    private fun initBoard() {
        board.removeAllViews()
        
        val boardWidth = Math.min(window.decorView.width - 40, 450)
        spacing = boardWidth / (gridSize - 1)
        boardContainer.layoutParams.width = boardWidth
        boardContainer.layoutParams.height = boardWidth
        
        for (r in 0 until gridSize) {
            for (c in 0 until gridSize) {
                val x = c * spacing
                val y = r * spacing
                
                // Create dot
                val dot = View(this).apply {
                    layoutParams = FrameLayout.LayoutParams(14, 14)
                    x = x
                    y = y
                    background = getDrawable(R.drawable.dot_background)
                }
                board.addView(dot)
                
                // Create touch area
                val touchArea = View(this).apply {
                    layoutParams = FrameLayout.LayoutParams(50, 50)
                    x = x
                    y = y
                    alpha = 0f
                }
                
                val onDown = { e: android.view.MotionEvent ->
                    e.preventDefault()
                    startDot = Dot(r, c, x, y)
                    previewLine.visibility = View.VISIBLE
                    previewLine.x = x
                    previewLine.y = y
                    previewLine.layoutParams.width = 0
                    previewLine.setBackgroundColor(players[currentPlayer]!!.color.toInt() or 0x66000000)
                }
                
                touchArea.setOnTouchListener { v, e ->
                    if (e.action == android.view.MotionEvent.ACTION_DOWN) {
                        onDown(e)
                        return@setOnTouchListener true
                    }
                    return@setOnTouchListener false
                }
                
                board.addView(touchArea)
            }
        }
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) initBoard()
    }

    override fun onBackPressed() {
        if (gameContainer.visibility == View.VISIBLE) {
            gameContainer.visibility = View.GONE
            setupScreen.visibility = View.VISIBLE
            drawnLines.clear()
        } else {
            super.onBackPressed()
        }
    }

    private fun updateScores() {
        scoreP1.text = players["p1"]?.score.toString()
        scoreP2.text = players["p2"]?.score.toString()
    }

    private fun updateTurnIndicator() {
        turnText.text = "دور: ${players[currentPlayer]?.name}"
    }

    private fun checkSnap(cx: Float, cy: Float): Boolean {
        val view = board.findViewWithTag<View>("dot-${cx}-${cy}")
        if (view != null) {
            val rect = view.getBoundingRect()
            val tr = Math.round((rect.top + rect.height() / 2f) / spacing)
            val tc = Math.round((rect.left + rect.width() / 2f) / spacing)
            
            val dr = Math.abs(startDot?.row ?: 0 - tr)
            val dc = Math.abs(startDot?.col ?: 0 - tc)
            
            if ((dr == 1 && dc == 0) || (dr == 0 && dc == 1)) {
                val type = if (dr == 0) "h" else "v"
                val r = if (type == "h") startDot?.row ?: 0 else Math.min(startDot?.row ?: 0, tr)
                val c = if (type == "h") Math.min(startDot?.col ?: 0, tc) else startDot?.col ?: 0
                val lineId = "$type-$r-$c"
                
                if (!drawnLines.contains(lineId)) {
                    createFinalLine(lineId, type, r, c)
                    return true
                }
            }
        }
        return false
    }

    private fun createFinalLine(lineId: String, type: String, r: Int, c: Int) {
        drawnLines.add(lineId)
        
        val line = View(this).apply {
            layoutParams = FrameLayout.LayoutParams(
                if (type == "h") spacing else 8,
                if (type == "h") 8 else spacing
            )
            x = if (type == "h") c * spacing else c * spacing - 4
            y = if (type == "h") r * spacing - 4 else r * spacing
            background = getDrawable(R.drawable.line_background)
            setBackgroundColor(players[currentPlayer]!!.color.toInt())
            tag = lineId
        }
        board.addView(line)
        
        // Simple square detection for demo
        checkForSquare(r, c, type)
        
        // Switch player
        currentPlayer = if (currentPlayer == "p1") "p2" else "p1"
        updateTurnIndicator()
    }

    private fun checkForSquare(row: Int, col: Int, type: String) {
        // Simplified square detection - complete in full implementation
        // Count lines around potential squares
        val horizontalLines = drawnLines.filter { it.startsWith("h-") }
        val verticalLines = drawnLines.filter { it.startsWith("v-") }
        
        // Check for completed squares (simplified)
        for (r in 0 until gridSize - 1) {
            for (c in 0 until gridSize - 1) {
                val top = "h-$r-${c}" in drawnLines
                val bottom = "h-${r + 1}-${c}" in drawnLines
                val left = "v-${r}-${c}" in drawnLines
                val right = "v-${r}-${c + 1}" in drawnLines
                
                if (top && bottom && left && right) {
                    // Player gets point for completing square
                    players[currentPlayer]?.score = (players[currentPlayer]?.score ?: 0) + 1
                    updateScores()
                    Toast.makeText(this, "${players[currentPlayer]?.name} سجل نقطة!", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // Clean up listeners
    }
}
