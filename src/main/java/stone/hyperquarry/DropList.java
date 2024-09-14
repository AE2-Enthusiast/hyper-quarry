package stone.hyperquarry;

import com.google.common.primitives.Doubles;
import com.google.common.primitives.Shorts;
import it.unimi.dsi.fastutil.ints.Int2DoubleMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import org.apache.commons.rng.sampling.distribution.AliasMethodDiscreteSampler;
import org.apache.commons.rng.sampling.distribution.SharedStateDiscreteSampler;
import org.apache.commons.rng.simple.RandomSource;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DropList {
	private static final Int2ObjectMap<DropList> MASTER_LISTS = new Int2ObjectOpenHashMap<>();
	
	private SharedStateDiscreteSampler sampler;

    public static final int COST_PER_ITEM = 20000;

    private int cost = COST_PER_ITEM;
	private double[] weights;
	private Item[] items;
	private short[] metas;

    private DropList(double[] weights, Item[] items, short[] metas) {
		this.weights = weights;
		this.items = items;
		this.metas = metas;
	}
	
    private DropList(double[] weights, Item[] items, short[] metas, int cost) {
        this(weights, items, metas);
        this.cost = cost;
    }

    public int init() {
		this.sampler = AliasMethodDiscreteSampler.of(RandomSource.XO_RO_SHI_RO_64_S.create(), weights);
        return cost;
	}

    public DropList filter(boolean isWhitelist, Set<Item> filter) {
        int size = isWhitelist ? filter.size() : items.length - filter.size();
        double[] newWeights = new double[size];
        Item[] newItems = new Item[size];
        short[] newMetas = new short[size];

        int oldIndex = 0;
        double skippedWeight = 0;
        double totalWeight = 0;
        for (int i = 0; i < size; i++)
        {
            // if it's a whitelist, and the filter contains it, don't increment
            // if it's a blacklist, and the filter doesn't contain it, don't increment
            while (isWhitelist ^ filter.contains(this.items[oldIndex]))
            {
                skippedWeight += this.weights[oldIndex];
                totalWeight += this.weights[oldIndex];
                oldIndex++;
            }
            totalWeight += this.weights[oldIndex];

            newWeights[i] = this.weights[oldIndex];
            newItems[i] = this.items[oldIndex];
            newMetas[i] = this.metas[oldIndex];
            oldIndex++;
        }
        
        return new DropList(newWeights, newItems, newMetas,
            (int) (this.cost / ((totalWeight - skippedWeight) / totalWeight)));
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
        ClassLoader classloader = Thread.currentThread().getContextClassLoader();
        InputStream is = classloader.getResourceAsStream("stats/drops_" + id + ".txt");
		Pattern pattern = Pattern.compile("(.+?):(.+?):(\\d+?)-(\\d\\.\\d+(?:E-?)?\\d+)");
		Map<Item, Int2DoubleMap> item2meta2weight = new HashMap<>();
        try (Scanner scanner = new Scanner(is))
        {
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
		}
	}
}
