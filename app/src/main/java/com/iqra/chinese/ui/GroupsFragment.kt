package com.iqra.chinese.ui

import android.os.Bundle
import android.view.*
import android.widget.EditText
import androidx.core.os.bundleOf
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.*
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.iqra.chinese.R
import com.iqra.chinese.data.StudyGroup
import com.iqra.chinese.databinding.FragmentGroupsBinding
import com.iqra.chinese.databinding.ItemGroupBinding

class GroupsFragment : BaseFragment() {
    private var _b: FragmentGroupsBinding? = null
    private val b get() = _b!!

    override fun onCreateView(i: LayoutInflater, c: ViewGroup?, s: Bundle?): View {
        _b = FragmentGroupsBinding.inflate(i, c, false); return b.root
    }
    override fun onViewCreated(view: View, s: Bundle?) {
        super.onViewCreated(view, s)
        val adapter = GroupAdapter(
            { g -> findNavController().navigate(R.id.action_groups_to_groupPractice, bundleOf("name" to g.name)) },
            { g -> MaterialAlertDialogBuilder(requireContext()).setTitle("Delete '${g.name}'?")
                .setPositiveButton("Delete") { _,_ -> vm.deleteGroup(g) }
                .setNegativeButton("Cancel", null).show() }
        )
        b.rv.layoutManager = LinearLayoutManager(requireContext()); b.rv.adapter = adapter
        vm.groupsLive.observe(viewLifecycleOwner) {
            adapter.submitList(it)
            b.tvEmpty.visibility = if (it.isEmpty()) View.VISIBLE else View.GONE
        }
        b.btnCreate.setOnClickListener {
            val et = EditText(requireContext()).apply { hint = "Group name…"; setPadding(48,32,48,32) }
            MaterialAlertDialogBuilder(requireContext()).setTitle("New Group").setView(et)
                .setPositiveButton("Create") { _,_ -> et.text.toString().trim().let { if (it.isNotEmpty()) vm.createGroup(it) } }
                .setNegativeButton("Cancel", null).show()
        }
    }
    override fun onDestroyView() { super.onDestroyView(); _b = null }
}

class GroupAdapter(
    private val onOpen: (StudyGroup) -> Unit,
    private val onDelete: (StudyGroup) -> Unit
) : ListAdapter<StudyGroup, GroupAdapter.VH>(object : DiffUtil.ItemCallback<StudyGroup>() {
    override fun areItemsTheSame(a: StudyGroup, b: StudyGroup) = a.name == b.name
    override fun areContentsTheSame(a: StudyGroup, b: StudyGroup) = a == b
}) {
    inner class VH(val vb: ItemGroupBinding) : RecyclerView.ViewHolder(vb.root)
    override fun onCreateViewHolder(p: ViewGroup, t: Int) =
        VH(ItemGroupBinding.inflate(LayoutInflater.from(p.context), p, false))
    override fun onBindViewHolder(h: VH, pos: Int) {
        val g = getItem(pos)
        h.vb.tvName.text  = g.name
        h.vb.tvCount.text = "${g.wordIds.size} words"
        h.vb.btnPractice.setOnClickListener { onOpen(g) }
        h.vb.btnDelete.setOnClickListener   { onDelete(g) }
    }
}
