package com.iqra.chinese.ui;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000.\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\u0010\u0002\n\u0002\b\u0005\n\u0002\u0010\b\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0003\u0018\u00002\u0012\u0012\u0004\u0012\u00020\u0002\u0012\b\u0012\u00060\u0003R\u00020\u00000\u0001:\u0001\u0011B-\u0012\u0012\u0010\u0004\u001a\u000e\u0012\u0004\u0012\u00020\u0002\u0012\u0004\u0012\u00020\u00060\u0005\u0012\u0012\u0010\u0007\u001a\u000e\u0012\u0004\u0012\u00020\u0002\u0012\u0004\u0012\u00020\u00060\u0005\u00a2\u0006\u0002\u0010\bJ\u001c\u0010\t\u001a\u00020\u00062\n\u0010\n\u001a\u00060\u0003R\u00020\u00002\u0006\u0010\u000b\u001a\u00020\fH\u0016J\u001c\u0010\r\u001a\u00060\u0003R\u00020\u00002\u0006\u0010\u000e\u001a\u00020\u000f2\u0006\u0010\u0010\u001a\u00020\fH\u0016R\u001a\u0010\u0007\u001a\u000e\u0012\u0004\u0012\u00020\u0002\u0012\u0004\u0012\u00020\u00060\u0005X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u001a\u0010\u0004\u001a\u000e\u0012\u0004\u0012\u00020\u0002\u0012\u0004\u0012\u00020\u00060\u0005X\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u0012"}, d2 = {"Lcom/iqra/chinese/ui/GroupAdapter;", "Landroidx/recyclerview/widget/ListAdapter;", "Lcom/iqra/chinese/data/StudyGroup;", "Lcom/iqra/chinese/ui/GroupAdapter$VH;", "onOpen", "Lkotlin/Function1;", "", "onDelete", "(Lkotlin/jvm/functions/Function1;Lkotlin/jvm/functions/Function1;)V", "onBindViewHolder", "h", "pos", "", "onCreateViewHolder", "p", "Landroid/view/ViewGroup;", "t", "VH", "app_debug"})
public final class GroupAdapter extends androidx.recyclerview.widget.ListAdapter<com.iqra.chinese.data.StudyGroup, com.iqra.chinese.ui.GroupAdapter.VH> {
    @org.jetbrains.annotations.NotNull()
    private final kotlin.jvm.functions.Function1<com.iqra.chinese.data.StudyGroup, kotlin.Unit> onOpen = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlin.jvm.functions.Function1<com.iqra.chinese.data.StudyGroup, kotlin.Unit> onDelete = null;
    
    public GroupAdapter(@org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function1<? super com.iqra.chinese.data.StudyGroup, kotlin.Unit> onOpen, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function1<? super com.iqra.chinese.data.StudyGroup, kotlin.Unit> onDelete) {
        super(null);
    }
    
    @java.lang.Override()
    @org.jetbrains.annotations.NotNull()
    public com.iqra.chinese.ui.GroupAdapter.VH onCreateViewHolder(@org.jetbrains.annotations.NotNull()
    android.view.ViewGroup p, int t) {
        return null;
    }
    
    @java.lang.Override()
    public void onBindViewHolder(@org.jetbrains.annotations.NotNull()
    com.iqra.chinese.ui.GroupAdapter.VH h, int pos) {
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u0012\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0004\b\u0086\u0004\u0018\u00002\u00020\u0001B\r\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\u0004R\u0011\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0005\u0010\u0006\u00a8\u0006\u0007"}, d2 = {"Lcom/iqra/chinese/ui/GroupAdapter$VH;", "Landroidx/recyclerview/widget/RecyclerView$ViewHolder;", "vb", "Lcom/iqra/chinese/databinding/ItemGroupBinding;", "(Lcom/iqra/chinese/ui/GroupAdapter;Lcom/iqra/chinese/databinding/ItemGroupBinding;)V", "getVb", "()Lcom/iqra/chinese/databinding/ItemGroupBinding;", "app_debug"})
    public final class VH extends androidx.recyclerview.widget.RecyclerView.ViewHolder {
        @org.jetbrains.annotations.NotNull()
        private final com.iqra.chinese.databinding.ItemGroupBinding vb = null;
        
        public VH(@org.jetbrains.annotations.NotNull()
        com.iqra.chinese.databinding.ItemGroupBinding vb) {
            super(null);
        }
        
        @org.jetbrains.annotations.NotNull()
        public final com.iqra.chinese.databinding.ItemGroupBinding getVb() {
            return null;
        }
    }
}