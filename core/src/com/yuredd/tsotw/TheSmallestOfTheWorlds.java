package com.yuredd.tsotw;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.graphics.g3d.model.Node;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.math.collision.Ray;
import com.badlogic.gdx.utils.Array;

import java.util.function.Consumer;

public class TheSmallestOfTheWorlds extends ApplicationAdapter implements InputProcessor {
	private SpriteBatch batch;
	private ModelBatch modelBatch;
	private PerspectiveCamera cam;
	private AssetManager assetManager;
	private Array<ModelInstance> instances = new Array<ModelInstance>();
	private Array<ModelInstance> collisions_instances = new Array<ModelInstance>();
	private Array<ModelInstance> heroPieces = new Array<ModelInstance>();
	private Array<ModelInstance> samples = new Array<ModelInstance>();
	private ModelInstance hero;
	private ModelInstance earth;
	private ModelInstance floor;
	private BitmapFont font;
	private Vector3 heroPosition = new Vector3();
	private Vector3 heroMovement = new Vector3();
	private Quaternion heroRotation = new Quaternion();
	private Sound engineSound;
	private long engineSoundID;
	private float heroRotationAngles;
	private boolean loading = true;

	private boolean collided = false;
	private boolean moving = false;

	private boolean leftMove = false;
	private boolean rightMove = false;
	private boolean forwardMove = false;
	private boolean backwardMove = false;


	@Override
	public void create () {
		modelBatch = new ModelBatch();
		batch = new SpriteBatch();
		Environment environment = new Environment();
		environment.set(new ColorAttribute(ColorAttribute.AmbientLight, 0.4f, 0.4f, 0.4f, 1f));
		environment.add(new DirectionalLight().set(0.8f, 0.8f, 0.8f, -1f, -0.8f, -0.2f));

		cam = new PerspectiveCamera(67, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		cam.up.set(0,1,0);
		cam.position.set(-1f, .2f, 0f)
				.add(-1f, 1f, 0);
		cam.lookAt(0f,0f,0f);
		cam.near = .2f;
		cam.far = 300f;
		cam.update();

		font = new BitmapFont();
		engineSound = Gdx.audio.newSound(Gdx.files.internal("engine.wav"));

		assetManager = new AssetManager();
		assetManager.load("assets.g3db", Model.class);
		loading = true;

		Gdx.input.setInputProcessor(this);
	}

	private void doneLoading() {
		Model model = assetManager.get("assets.g3db", Model.class);
		for (int i = 0; i < model.nodes.size; i++) {
			String id = model.nodes.get(i).id;
			ModelInstance instance = new ModelInstance(model, id);
			Node node = instance.getNode(id);

			/*
			instance.transform.set(node.globalTransform);
			node.translation.set(0,0,0);
			node.scale.set(1,1,1);
			node.rotation.idt();
			instance.calculateTransforms();
			*/

			if (id.startsWith("Curiosity_"))
				heroPieces.add(instance);
			else if(id.equals("Earth"))
				earth = instance;
			else if(id.equals("Floor"))
				floor = instance;
			else if(id.startsWith("Sample"))
				samples.add(instance);
			else if (id.startsWith("Collision_"))
				collisions_instances.add(instance);
			else if(!id.startsWith("Curiosity_"))
				instances.add(instance);
		}

		cam.position.set(heroPosition)
				.add(-5f, 5f, 0);
		cam.lookAt(heroPosition);
		cam.update();

		loading = false;
	}

	@Override
	public void render () {
		if (loading && assetManager.update())
			doneLoading();


		moving = false;

		if (!loading) {
			heroMovement = new Vector3();
			if (leftMove) {
				engineSoundID = engineSound.play(.05f);
				engineSound.setLooping(engineSoundID, true);
				moving = true;
				heroRotationAngles = heroRotationAngles + 3;
				//hero.transform.rotate(0,1,0,7);
			}
			if (rightMove) {
				engineSoundID = engineSound.play(.05f);
				engineSound.setLooping(engineSoundID, true);
				moving = true;
				heroRotationAngles = heroRotationAngles - 3;
				//hero.transform.rotate(0,1,0,-7);
			}
			if (forwardMove) {
				engineSoundID = engineSound.play(.05f);
				engineSound.setLooping(engineSoundID, true);
				moving = true;
				heroMovement = new Vector3(.1f,0,0).rotate(heroRotationAngles, 0,1,0);
				Vector3 heroPositionTo = heroPosition;
				heroPositionTo.add(heroMovement);

				collided = false;

				if (collisions_instances.size > 0) {
					for (ModelInstance collisions_instance : collisions_instances) {
						Ray collisionRay = new Ray(heroPosition, heroMovement);
						BoundingBox bb = new BoundingBox();
						collisions_instance.calculateBoundingBox(bb);

						if (bb.contains(heroPositionTo)) {
							heroMovement = new Vector3(0,0,0);
							collided = true;
							moving = false;
						}
					}
				}

				if (!collided) {
					heroPosition = heroPosition.add(heroMovement);
				}
			}
			if (backwardMove) {
				engineSoundID = engineSound.play(.05f);
				engineSound.setLooping(engineSoundID, true);
				moving = true;
				heroMovement = new Vector3(-.05f,0,0).rotate(heroRotationAngles, 0,1,0);

				Vector3 heroPositionTo = heroPosition;
				heroPositionTo.add(heroMovement);

				collided = false;

				if (collisions_instances.size > 0) {
					for (ModelInstance collisions_instance : collisions_instances) {
						Ray collisionRay = new Ray(heroPosition, heroMovement);
						BoundingBox bb = new BoundingBox();
						collisions_instance.calculateBoundingBox(bb);

						if (bb.contains(heroPositionTo)) {
							heroMovement = new Vector3(0,0,0);
							collided = true;
							moving = false;
						}
					}
				}

				if (!collided) {
					heroPosition = heroPosition.add(heroMovement);
				}
			}

			if(!moving) {
				engineSound.setLooping(engineSoundID, false);
				engineSound.stop();
			} else {
				for (ModelInstance heroPiece : heroPieces) {
					heroPiece.transform.setToTranslation(heroPosition);
					heroPiece.transform.rotate(0,1,0,heroRotationAngles+90);
				}
				cam.position.set(heroPosition)
						.add(-5f, 5f, 0);
				cam.lookAt(heroPosition);
				cam.update();
			}

		}
		if (heroRotationAngles > 360) heroRotationAngles = heroRotationAngles - 360;
		if (heroRotationAngles < 0) heroRotationAngles = heroRotationAngles + 360;

		Gdx.gl.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

		modelBatch.begin(cam);
		if (!loading) {
			modelBatch.render(floor);
			modelBatch.render(instances);
			modelBatch.render(samples);
			modelBatch.render(collisions_instances);
			if (heroPieces != null) modelBatch.render(heroPieces);

		}


		modelBatch.end();

		batch.begin();
		font.draw(batch, "Version 0.5a pos: " + heroPosition.toString() + " collided: " + collided, 10, 20);
		batch.end();
	}

	@Override
	public void dispose () {
		modelBatch.dispose();
	}

	@Override
	public boolean keyDown(int keycode) {
		switch	(keycode) {
			case Input.Keys.W:
				if (forwardMove) forwardMove = false;
				forwardMove = true;
				break;
			case Input.Keys.A:
				if (leftMove) leftMove = false;
				leftMove = true;
				break;
			case Input.Keys.S:
				if (backwardMove) backwardMove = false;
				backwardMove = true;
				break;
			case Input.Keys.D:
				if (rightMove) rightMove = false;
				rightMove = true;
				break;
		}
		return false;
	}

	@Override
	public boolean keyUp(int keycode) {
		switch	(keycode) {
			case Input.Keys.W:
				forwardMove = false;
				break;
			case Input.Keys.A:
				leftMove = false;
				break;
			case Input.Keys.S:
				backwardMove = false;
				break;
			case Input.Keys.D:
				rightMove = false;
				break;
		}
		return false;
	}

	@Override
	public boolean keyTyped(char character) {
		return false;
	}

	@Override
	public boolean touchDown(int screenX, int screenY, int pointer, int button) {
		return false;
	}

	@Override
	public boolean touchUp(int screenX, int screenY, int pointer, int button) {
		return false;
	}

	@Override
	public boolean touchDragged(int screenX, int screenY, int pointer) {
		return false;
	}

	@Override
	public boolean mouseMoved(int screenX, int screenY) {
		return false;
	}

	@Override
	public boolean scrolled(int amount) {
		return false;
	}
}
