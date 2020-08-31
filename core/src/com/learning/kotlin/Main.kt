package com.learning.kotlin

import com.badlogic.gdx.ApplicationAdapter
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.Circle
import com.badlogic.gdx.math.Rectangle
import java.util.*

class Main : ApplicationAdapter() {

    private lateinit var batch: SpriteBatch

    // Textures
    private lateinit var background: Texture
    private lateinit var pipeAbove: Texture
    private lateinit var pipeBelow: Texture
    private lateinit var birds: Array<Texture>

    // Shapes for collisions
    private lateinit var shapeRenderer: ShapeRenderer
    private lateinit var birdShape: Circle
    private lateinit var pipeAboveShape: Rectangle
    private lateinit var pipeBelowShape: Rectangle

    // Configuration Attributes
    private var score = 0
    private var birdPositionX = 50f
    private var birdPositionY = 0f
    private var screenWidth = 0f
    private var screenHeight = 0f
    private var animationIndex = 0f
    private var gravity = 0f
    private var pipePositionX = 0f
    private var pipePositionY = 0f
    private var spaceBetweenPipes = 180f
    private var passedThroughPipe = false

    private var justTouched = false

    private lateinit var scoreText: BitmapFont

    private var random: Random = Random()

    override fun create() {
        initTextures()
        initConfiguration()
        initShapes()
    }

    override fun render() {
        verifyGameState()
        evaluateScore()
        drawTextures()
        detectCollisions()
    }

    override fun dispose() {
    }

    private fun initConfiguration() {
        screenWidth = Gdx.graphics.width.toFloat()
        screenHeight = Gdx.graphics.height.toFloat()

        birdPositionY = screenHeight / 2

        pipePositionX = screenWidth

        scoreText.color = Color.WHITE
        scoreText.data.scale(10f)
    }

    private fun initShapes() {
        shapeRenderer = ShapeRenderer()
        birdShape = Circle()
        pipeAboveShape = Rectangle()
        pipeBelowShape = Rectangle()
    }

    private fun initTextures() {
        batch = SpriteBatch()
        scoreText = BitmapFont()

        birds = arrayOf(Texture("passaro1.png"),
                Texture("passaro2.png"),
                Texture("passaro3.png"))

        background = Texture("fundo.png")

        pipeAbove = Texture("cano_topo_maior.png")
        pipeBelow = Texture("cano_baixo_maior.png")
    }

    private fun drawTextures() {
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)

        batch.begin()

        batch.draw(background, 0f, 0f, screenWidth, screenHeight)
        batch.draw(birds[animationIndex.toInt()], birdPositionX, birdPositionY)
        batch.draw(pipeAbove, pipePositionX, screenHeight - pipeAbove.height + spaceBetweenPipes / 2 + pipePositionY)
        batch.draw(pipeBelow, pipePositionX, screenHeight / 2 - pipeBelow.height - spaceBetweenPipes / 2 + pipePositionY)

        scoreText.draw(batch,"$score", screenWidth / 2, screenHeight - 110)

        batch.end()
    }

    private fun detectCollisions() {
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line)
        shapeRenderer.color = Color.RED

        val birdWidth = birds.first().width
        val birdHeight = birds.first().height

        shapeRenderer.circle(birdPositionX + birdWidth / 2f, birdPositionY + birdHeight / 2f, birdWidth / 2f)

        shapeRenderer.end()
    }

    private fun verifyGameState() {
        val deltaTime = Gdx.app.graphics.deltaTime
        justTouched = Gdx.input.justTouched()

        if (justTouched) {
            gravity = -25f
        }

        pipePositionX -= deltaTime * 200
        if (pipePositionX < -pipeAbove.width) {
            pipePositionX = screenWidth
            pipePositionY = (random.nextInt(800) - 400).toFloat()
            passedThroughPipe = false
        }

        applyGravity()

        animationIndex += deltaTime * 10

        if (animationIndex >= birds.size) {
            animationIndex = 0f
        }

        calculatePhysics()
    }

    private fun evaluateScore() {
        if (pipePositionX < 50 - birds.first().width && !passedThroughPipe) {
            passedThroughPipe = true
            score++
        }
    }

    private fun applyGravity() {
        if (birdPositionY > 0 || justTouched) {
            birdPositionY -= gravity
        }
    }

    private fun calculatePhysics() {
        gravity++
    }
}