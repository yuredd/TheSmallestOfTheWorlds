package com.yuredd.tsotw;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.graphics.g3d.model.Node;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;

public class TheSmallestOfTheWorlds extends ApplicationAdapter implements InputProcessor {
	private ModelBatch modelBatch;
	private PerspectiveCamera cam;
	private AssetManager assetManager;
	private Array<ModelInstance> instances = new Array<ModelInstance>();
	private Array<ModelInstance> hero = new Array<ModelInstance>();
	private Vector3 heroPosition = new Vector3();
	private Vector3 heroDirection = new Vector3();
	private float heroRotation;
	private boolean loading = true;

	private boolean leftMove = false;
	private boolean rightMove = false;
	private boolean forwardMove = false;
	private boolean backwardMove = false;


	@Override
	public void create () {
		modelBatch = new ModelBatch();
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

			instance.transform.set(node.globalTransform);
			node.translation.set(0,0,0);
			node.scale.set(1,1,1);
			node.rotation.idt();
			instance.calculateTransforms();

			if (id.startsWith("Curiosity_"))
				hero.add(instance);
			else
				instances.add(instance);
		}

		loading = false;
	}

	@Override
	public void render () {
		if (loading && assetManager.update())
			doneLoading();

		if (!loading) {
			heroDirection = new Vector3(0,0,0);

			Vector3 forwardVec = new Vector3(0,0,.1f).rotate(heroRotation, 0,1,0);
			Vector3 backwardVec = new Vector3(0,0,-.02f).rotate(heroRotation, 0,1,0);

			if (forwardMove) {
				for (ModelInstance instance : hero) {
					instance.transform.trn(forwardVec);
				}
			}
			if (leftMove) heroRotation = heroRotation + 10;
			if (backwardMove) {
				for (ModelInstance instance : hero) {
					instance.transform.trn(backwardVec);
				}
			}
			if (rightMove) heroRotation = heroRotation - 10;

			for (ModelInstance instance : hero) {
				instance.transform.rotate(0,0,1,heroRotation).trn(heroPosition);
			}

			cam.position.set(heroPosition)
					.add(-5f, 5f, 0);
			cam.lookAt(heroPosition);
			cam.update();
		}

		Gdx.gl.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

		modelBatch.begin(cam);
		if (!loading) {
			modelBatch.render(instances);
			if (hero != null) modelBatch.render(hero);

		}
		modelBatch.end();
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
