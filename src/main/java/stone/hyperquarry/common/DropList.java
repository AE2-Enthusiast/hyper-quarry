package stone.hyperquarry.common;

import com.github.bsideup.jabel.Desugar;
import com.google.common.primitives.Doubles;
import com.google.common.primitives.Shorts;
import it.unimi.dsi.fastutil.ints.Int2DoubleMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

import org.apache.commons.lang3.tuple.Triple;
import org.apache.commons.rng.sampling.distribution.AliasMethodDiscreteSampler;
import org.apache.commons.rng.sampling.distribution.SharedStateDiscreteSampler;
import org.apache.commons.rng.simple.RandomSource;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NavigableSet;
import java.util.Scanner;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.DoubleStream;

@Desugar
public record DropList(Item[] items, short[] metas, double[] weights, int cost) {

    private static final Map<String, DropList> MASTER_LISTS = new HashMap<>();

    public static final int COST_PER_ITEM = 20000;

    /**
     * Initializes this drop list to start generating drops
     * 
     * @return the cost, in RF, of generating a single item
     */
    public SharedStateDiscreteSampler init() {
        return AliasMethodDiscreteSampler.of(RandomSource.XO_RO_SHI_RO_64_S.create(), weights);
    }

    /**
     * Generates a random stack with the given count of items
     * 
     * @param index the index of the item to make the stack of (see init())
     * @param count the stack size of the item stack
     * @return the stack randomly generated based on the weights
     */
    public ItemStack getStack(int index, byte count) {
        return new ItemStack(this.items[index], count, this.metas[index]);
    }

    /**
     * Creates a new drop list
     * 
     * Drop lists are cached and will be retrieved from disk as needed
     * 
     * @param dimension the numerical id of the dimension
     * @return
     */
    public static DropList of(String enchants, int dimension) {
        return MASTER_LISTS.computeIfAbsent(enchants + '_' + dimension, (id) -> {
            return loadDimensionDrops(id);
        });
    }

    /**
     * Creates a new drop list and automatically filters it based on the filter
     * 
     * @param dimension the numerical id of the dimension
     * @param filter    a bit set where each bit corresponds to a item being
     *                  included
     * @return the new drop list, null if filter was invalid
     */
    public static DropList of(Filter filter) {
        DropList maybeNull = of(filter.enchants(), filter.dimension());
            return maybeNull != null ? maybeNull.filter(filter.mask()) : null;
    }

    private static DropList loadDimensionDrops(String id) {
        ClassLoader classloader = Thread.currentThread().getContextClassLoader();
        InputStream is = classloader.getResourceAsStream("stats/" + id + ".txt");
        if (is == null)
            return null;
        Pattern pattern = Pattern.compile("(.+?):(.+?):(\\d+?)-(\\d\\.\\d+(?:E-?)?\\d+)");
        Map<Item, Int2DoubleMap> item2meta2weight = new HashMap<>();
        try (Scanner scanner = new Scanner(is)) {
            NavigableSet<Triple<Item, Short, Double>> entries = new TreeSet<>((triple1, triple2) -> {
                double diff = triple1.getRight() - triple2.getRight();
                return diff > 0 ? 1 : diff < 0 ? -1 : 0;
            });
            while (scanner.hasNextLine()) {
                Matcher matcher = pattern.matcher(scanner.nextLine());
                if (matcher.find()) {
                    Item item = Item.REGISTRY.getObject(new ResourceLocation(matcher.group(1), matcher.group(2)));
                    short meta = Short.valueOf(matcher.group(3));
                    double weight = Double.valueOf(matcher.group(4));
                    entries.add(Triple.of(item, meta, weight));
                }
            }

            return DropList.of(entries);
        }
    }

    public static DropList of(NavigableSet<Triple<Item, Short, Double>> entries) {
        int length = entries.size();
        Item[] items = new Item[length];
        short[] metas = new short[length];
        double[] weights = new double[length];
        for (int i = 0; i < length; i++) {
            Triple<Item, Short, Double> entry = entries.pollLast();
            items[i] = entry.getLeft();
            metas[i] = entry.getMiddle();
            weights[i] = entry.getRight();
        }

        return new DropList(items, metas, weights, COST_PER_ITEM);
    }

    private DropList filter(BitSet mask) {
        double[] weights = new double[this.weights.length];
        for (int i = mask.nextSetBit(0); i != -1; i = mask.nextSetBit(i + 1)) {
            weights[i] = this.weights[i];
        }

        double oldTotal = DoubleStream.of(this.weights).sum();
        double newTotal = DoubleStream.of(weights).sum();

        return new DropList(this.items, this.metas, weights, (int) (this.cost / (newTotal / oldTotal)));
    }
}
