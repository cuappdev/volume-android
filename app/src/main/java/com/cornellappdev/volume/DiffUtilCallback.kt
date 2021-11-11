
import androidx.annotation.Nullable
import androidx.recyclerview.widget.DiffUtil
import com.cornellappdev.volume.models.Article


class MyDiffCallback(private val oldArticles: List<Article>, private val newArticles: List<Article>) :
    DiffUtil.Callback() {

    override fun getOldListSize(): Int {
        return oldArticles.size
    }

    override fun getNewListSize(): Int {
        return newArticles.size
    }

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldArticles[oldItemPosition].id === newArticles[newItemPosition].id
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldArticles[oldItemPosition] == newArticles[newItemPosition]
    }

    @Nullable
    override fun getChangePayload(oldItemPosition: Int, newItemPosition: Int): Any? {
        //you can return particular field for changed item.
        return super.getChangePayload(oldItemPosition, newItemPosition)
    }
}