package stone.hyperquarry;

import com.google.common.primitives.Doubles;
import com.google.common.primitives.Shorts;
import it.unimi.dsi.fastutil.ints.Int2DoubleMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.server.FMLServerHandler;
import org.apache.commons.rng.sampling.distribution.AliasMethodDiscreteSampler;
import org.apache.commons.rng.sampling.distribution.SharedStateDiscreteSampler;
import org.apache.commons.rng.simple.RandomSource;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DropList {
	private static final Int2ObjectMap<DropList> MASTER_LISTS = new Int2ObjectOpenHashMap<>();
	
	private SharedStateDiscreteSampler sampler;

	private double[] weights;
	private Item[] items;
	private short[] metas;

	private DropList(double[] weights, Item[] items, short[] metas) {
		this.weights = weights;
		this.items = items;
		this.metas = metas;
	}
	
	public void init() {
		this.sampler = AliasMethodDiscreteSampler.of(RandomSource.XO_RO_SHI_RO_64_S.create(), weights);
	}

	private ItemStack getStack(int index, byte count) {
		return new ItemStack(this.items[index], count, this.metas[index]);
	}
	
	public ItemStack getStack(byte count) {
		return getStack(sampler.sample(), count);
	}

	public static DropList of(int dimension) {
		return MASTER_LISTS.computeIfAbsent(dimension, (id) -> {
            return loadDimensionDrops(id);
		});
	}

	private static DropList loadDimensionDrops(int id) {
		File root = FMLServerHandler.instance().getSavesDirectory();
		Pattern pattern = Pattern.compile("(.+?):(.+?):(\\d+?)-(\\d\\.\\d+(?:e-?)?\\d+)");
		File file = new File(root, "drops_" + id + ".txt");
		Map<Item, Int2DoubleMap> item2meta2weight = new HashMap<>();
		try (Scanner scanner = new Scanner(file)) {
			List<Double> weights = new ArrayList<>();
			List<Item> items = new ArrayList<>();
			List<Short> metas = new ArrayList<>();
			while (scanner.hasNextLine()) {
				Matcher matcher = pattern.matcher(scanner.nextLine());
				if (matcher.find()) {
					items.add(Item.REGISTRY.getObject(new ResourceLocation(matcher.group(1), matcher.group(2))));
					metas.add(Short.valueOf(matcher.group(3)));
					weights.add(Double.valueOf(matcher.group(4)));
				}
			}
            return new DropList(Doubles.toArray(weights),
                items.stream().toArray(Item[]::new), Shorts.toArray(metas));
		} catch (FileNotFoundException e) {
			return null;
		}
	}
}
