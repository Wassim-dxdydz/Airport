"use client";

import { useEffect, useMemo, useState } from "react";
import { Button } from "@/components/ui/button";
import {
    Dialog,
    DialogContent,
    DialogDescription,
    DialogFooter,
    DialogHeader,
    DialogTitle,
} from "@/components/ui/dialog";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import {
    Select,
    SelectContent,
    SelectItem,
    SelectTrigger,
    SelectValue,
} from "@/components/ui/select";
import {
    Table,
    TableBody,
    TableCell,
    TableHead,
    TableHeader,
    TableRow,
} from "@/components/ui/table";
import { Badge } from "@/components/ui/badge";
import { Card } from "@/components/ui/card";
import {
    DropdownMenu,
    DropdownMenuContent,
    DropdownMenuItem,
    DropdownMenuTrigger,
} from "@/components/ui/dropdown-menu";
import {
    MoreHorizontal,
    Plus,
    Pencil,
    Trash2,
    RefreshCw,
    Plane,
    Link2,
    Unlink2,
    Flag,
} from "lucide-react";

const API_BASE = process.env.NEXT_PUBLIC_API_BASE ?? "";

const VOL_ETATS = [
    "PREVU",
    "EN_ATTENTE",
    "EMBARQUEMENT",
    "DECOLLE",
    "EN_VOL",
    "ARRIVE",
    "ANNULE"
] as const;

type Vol = {
    id: string;
    numeroVol: string;
    origine: string;
    destination: string;
    heureDepart: string;
    heureArrivee: string;
    etat: string;
    avionId?: string | null;
    createdAt?: string | null;
    updatedAt?: string | null;
};

type CreateVolPayload = {
    numeroVol: string;
    origine: string;
    destination: string;
    heureDepart: string;
    heureArrivee: string;
};

type UpdateVolPayload = Partial<{
    origine: string;
    destination: string;
    heureDepart: string;
    heureArrivee: string;
    etat: string;
    avionId: string | null;
}>;

async function api<T>(path: string, init?: RequestInit): Promise<T> {
    const res = await fetch(`${API_BASE}${path}`, {
        ...init,
        headers: { "Content-Type": "application/json", ...(init?.headers || {}) },
        cache: "no-store",
    });
    if (!res.ok) {
        const text = await res.text().catch(() => "");
        throw new Error(text || `HTTP ${res.status}`);
    }
    return res.json();
}

async function loadHistory(volId: string) {
    return api<{ id: string | null; etat: string; changedAt: string }[]>(
        `/api/vols/${volId}/history`
    );
}

const fmt = new Intl.DateTimeFormat("fr-FR", {
    dateStyle: "short",
    timeStyle: "short",
});
function toLocalDateTimeInputValue(isoLike: string) {
    // expects "YYYY-MM-DDTHH:mm[:ss]" -> "YYYY-MM-DDTHH:mm"
    return isoLike?.slice(0, 16) ?? "";
}
function fromInputToLocalDateTime(value: string) {
    // input "YYYY-MM-DDTHH:mm" -> add seconds
    if (!value) return value;
    return value.length === 16 ? `${value}:00` : value;
}

// ---------- Page ----------
export default function VolPage() {
    const [vols, setVols] = useState<Vol[]>([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState<string | null>(null);

    const [etatFilter, setEtatFilter] = useState<string>("ALL");

    const [openCreate, setOpenCreate] = useState(false);
    const [openEdit, setOpenEdit] = useState<Vol | null>(null);
    const [openEtat, setOpenEtat] = useState<{ id: string; etat: string } | null>(null);
    const [openAssign, setOpenAssign] = useState<{ id: string } | null>(null);

    const [createForm, setCreateForm] = useState<CreateVolPayload>({
        numeroVol: "",
        origine: "",
        destination: "",
        heureDepart: "",
        heureArrivee: "",
    });

    const [editForm, setEditForm] = useState<UpdateVolPayload>({
        origine: "",
        destination: "",
        heureDepart: "",
        heureArrivee: "",
        etat: undefined,
        avionId: undefined,
    });

    const [etatForm, setEtatForm] = useState<string>("PLANIFIE");
    const [assignForm, setAssignForm] = useState<string>("");

    const [openHistory, setOpenHistory] = useState<{ id: string } | null>(null);
    const [historyList, setHistoryList] = useState<
        { id: string | null; etat: string; changedAt: string }[]
    >([]);

    const [viewMode, setViewMode] = useState<"ALL" | "SORTANT" | "ENTRANT">("ALL");
    const AIRPORT = "ATL";

    const load = async () => {
        try {
            setLoading(true);
            setError(null);

            if (viewMode === "SORTANT") {
                const all = await api<Vol[]>("/api/vols");
                setVols(all.filter(v => v.origine === AIRPORT));
                return;
            }

            if (viewMode === "ENTRANT") {
                const internal = await api<Vol[]>("/api/vols");
                const extern = await api<any[]>(`http://129.88.210.74:8080/api/volExterieurs/${AIRPORT}`);

                const externMapped: Vol[] = extern.map(e => ({
                    id: "-",
                    numeroVol: e.numeroVol,
                    origine: e.origine,
                    destination: e.destination,
                    heureDepart: e.heureDepart,
                    heureArrivee: e.heureArrivee,
                    etat: e.etat,
                    avionId: null
                }));

                const incoming = [
                    ...internal.filter(v => v.destination === AIRPORT),
                    ...externMapped
                ];

                setVols(incoming);
                return;
            }
            const data = await api<Vol[]>("/api/vols");
            setVols(data);
        } catch (e: any) {
            setError(e.message || "Erreur de chargement");
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        load();
    }, [etatFilter]);

    const badgeForEtat = (etat: string) => {
        const e = etat.toUpperCase();
        if (e === "PREVU") return <Badge className="bg-blue-600">{etat}</Badge>;
        if (e === "EN_ATTENTE") return <Badge className="bg-yellow-600">{etat}</Badge>;
        if (e === "EMBARQUEMENT") return <Badge className="bg-purple-600">{etat}</Badge>;
        if (e === "DECOLLE") return <Badge className="bg-orange-600">{etat}</Badge>;
        if (e === "EN_VOL") return <Badge className="bg-green-600">{etat}</Badge>;
        if (e === "ARRIVE") return <Badge className="bg-gray-600">{etat}</Badge>;
        if (e === "ANNULE") return <Badge className="bg-red-600">{etat}</Badge>;
        return <Badge variant="secondary">{etat}</Badge>;
    };

    // actions
    const createVol = async () => {
        const payload: CreateVolPayload = {
            numeroVol: createForm.numeroVol.trim(),
            origine: createForm.origine.trim(),
            destination: createForm.destination.trim(),
            heureDepart: fromInputToLocalDateTime(createForm.heureDepart),
            heureArrivee: fromInputToLocalDateTime(createForm.heureArrivee),
        };
        await api<Vol>("/api/vols", { method: "POST", body: JSON.stringify(payload) });
        setOpenCreate(false);
        setCreateForm({ numeroVol: "", origine: "", destination: "", heureDepart: "", heureArrivee: "" });
        await load();
    };

    const startEdit = (v: Vol) => {
        setOpenEdit(v);
        setEditForm({
            origine: v.origine,
            destination: v.destination,
            heureDepart: toLocalDateTimeInputValue(v.heureDepart),
            heureArrivee: toLocalDateTimeInputValue(v.heureArrivee),
            etat: v.etat,
            avionId: v.avionId ?? undefined,
        });
    };

    const updateVol = async (id: string) => {
        const payload: UpdateVolPayload = {
            origine: editForm.origine?.trim(),
            destination: editForm.destination?.trim(),
            heureDepart: editForm.heureDepart ? fromInputToLocalDateTime(editForm.heureDepart) : undefined,
            heureArrivee: editForm.heureArrivee ? fromInputToLocalDateTime(editForm.heureArrivee) : undefined,
            etat: editForm.etat,
            avionId: editForm.avionId ?? null,
        };
        await api<Vol>(`/api/vols/${id}`, { method: "PATCH", body: JSON.stringify(payload) });
        setOpenEdit(null);
        await load();
    };

    const deleteVol = async (id: string) => {
        if (!confirm("Supprimer ce vol ?")) return;
        await fetch(`${API_BASE}/api/vols/${id}`, { method: "DELETE" });
        await load();
    };

    const changeEtat = async () => {
        if (!openEtat) return;
        // Backend attend un body = VolEtat (string JSON, pas d'objet)
        await fetch(`${API_BASE}/api/vols/${openEtat.id}/etat`, {
            method: "PATCH",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify(etatForm), // => "EN_COURS"
        }).then((r) => {
            if (!r.ok) throw new Error("Erreur mise à jour état");
        });
        setOpenEtat(null);
        await load();
    };

    const assignAvion = async () => {
        if (!openAssign || !assignForm) return;
        await api<Vol>(`/api/vols/${openAssign.id}/assign-avion/${assignForm}`, { method: "POST" });
        setOpenAssign(null);
        setAssignForm("");
        await load();
    };

    const unassignAvion = async (id: string) => {
        await api<Vol>(`/api/vols/${id}/unassign-avion`, { method: "POST" });
        await load();
    };

    const colorForEtat = (etat: string) => {
        const e = etat.toUpperCase();
        if (e.includes("PLAN")) return "bg-blue-600";
        if (e.includes("COURS") || e.includes("VOL")) return "bg-green-600";
        if (e.includes("RET")) return "bg-yellow-500";
        if (e.includes("ANNU")) return "bg-red-600";
        if (e.includes("TERM")) return "bg-gray-600";
        return "bg-slate-400";
    };

    return (
        <div className="mx-auto max-w-6xl px-4 py-8 space-y-6">
            <div className="flex flex-wrap items-center justify-between gap-3">
                <div>
                    <h1 className="text-2xl font-semibold">Vols</h1>
                    <p className="text-muted-foreground">Planification, suivi et affectation avion.</p>
                </div>

                <div className="flex flex-wrap items-center gap-2">
                    <Button
                        variant={viewMode === "ALL" ? "default" : "outline"}
                        onClick={() => setViewMode("ALL")}
                    >
                        Tous
                    </Button>

                    <Button
                        variant={viewMode === "SORTANT" ? "default" : "outline"}
                        onClick={() => setViewMode("SORTANT")}
                    >
                        Sortants
                    </Button>

                    <Button
                        variant={viewMode === "ENTRANT" ? "default" : "outline"}
                        onClick={() => setViewMode("ENTRANT")}
                    >
                        Entrants
                    </Button>

                    <Select value={etatFilter} onValueChange={setEtatFilter}>
                        <SelectTrigger className="w-[200px]">
                            <SelectValue placeholder="Filtrer par état" />
                        </SelectTrigger>
                        <SelectContent>
                            <SelectItem value="ALL">Tous les états</SelectItem>
                            {VOL_ETATS.map((e) => (
                                <SelectItem key={e} value={e}>{e}</SelectItem>
                            ))}
                        </SelectContent>
                    </Select>

                    <Button variant="outline" onClick={load}>
                        <RefreshCw className="mr-2 h-4 w-4" />
                        Actualiser
                    </Button>

                    {/* Always visible now */}
                    <Button onClick={() => setOpenCreate(true)}>
                        <Plus className="mr-2 h-4 w-4" />
                        Nouveau vol
                    </Button>
                </div>
            </div>

            {error && (
                <Card className="border-red-300 bg-red-50 p-4 text-red-800">
                    {error}
                </Card>
            )}

            {/* Table */}
            <Card className="overflow-hidden py-0">
                <div className="overflow-x-auto px-4 py-2">
                    <Table>
                        <TableHeader>
                            <TableRow>
                                <TableHead>Numéro</TableHead>
                                <TableHead>Origine → Destination</TableHead>
                                <TableHead>Départ</TableHead>
                                <TableHead>Arrivée</TableHead>
                                <TableHead>État</TableHead>
                                <TableHead>Avion</TableHead>
                                <TableHead className="text-right">Actions</TableHead>
                            </TableRow>
                        </TableHeader>
                        <TableBody>
                            {loading ? (
                                <TableRow>
                                    <TableCell colSpan={7}>Chargement…</TableCell>
                                </TableRow>
                            ) : vols.length === 0 ? (
                                <TableRow>
                                    <TableCell colSpan={7} className="text-muted-foreground">
                                        Aucun vol
                                    </TableCell>
                                </TableRow>
                            ) : (
                                vols.map((v) => (
                                    <TableRow key={v.id}>
                                        <TableCell className="font-medium">{v.numeroVol}</TableCell>
                                        <TableCell>{v.origine} → {v.destination}</TableCell>
                                        <TableCell>{fmt.format(new Date(v.heureDepart))}</TableCell>
                                        <TableCell>{fmt.format(new Date(v.heureArrivee))}</TableCell>
                                        <TableCell>{badgeForEtat(v.etat)}</TableCell>
                                        <TableCell className="font-mono text-xs">
                                            {v.avionId ?? <span className="text-muted-foreground">—</span>}
                                        </TableCell>
                                        <TableCell className="text-right">
                                            <DropdownMenu>
                                                <DropdownMenuTrigger asChild>
                                                    <div>
                                                        <Button size="icon" variant="ghost">
                                                            <MoreHorizontal className="h-4 w-4" />
                                                        </Button>
                                                    </div>
                                                </DropdownMenuTrigger>
                                                <DropdownMenuContent
                                                    align="end"
                                                    sideOffset={5}
                                                    onCloseAutoFocus={(e) => e.preventDefault()}
                                                    onInteractOutside={(e) => e.preventDefault()}
                                                >
                                                <DropdownMenuItem onClick={() => setOpenEtat({ id: v.id, etat: v.etat })}>
                                                        <Flag className="mr-2 h-4 w-4" /> Changer l’état
                                                    </DropdownMenuItem>
                                                    <DropdownMenuItem onClick={() => setOpenEdit(v)}>
                                                        <Pencil className="mr-2 h-4 w-4" /> Modifier
                                                    </DropdownMenuItem>
                                                    {v.avionId ? (
                                                        <DropdownMenuItem onClick={() => unassignAvion(v.id)}>
                                                            <Unlink2 className="mr-2 h-4 w-4" /> Retirer l’avion
                                                        </DropdownMenuItem>
                                                    ) : (
                                                        <DropdownMenuItem onClick={() => setOpenAssign({ id: v.id })}>
                                                            <Link2 className="mr-2 h-4 w-4" /> Assigner un avion
                                                        </DropdownMenuItem>
                                                    )}
                                                    <DropdownMenuItem
                                                        onClick={async () => {
                                                            try {
                                                                const data = await loadHistory(v.id);
                                                                setHistoryList(data);
                                                                setOpenHistory({ id: v.id });
                                                            } catch (e: any) {
                                                                alert("Erreur chargement historique");
                                                            }
                                                        }}
                                                    >
                                                        <Plane className="mr-2 h-4 w-4" /> Historique
                                                    </DropdownMenuItem>
                                                    <DropdownMenuItem
                                                        className="text-red-600"
                                                        onClick={() => deleteVol(v.id)}
                                                    >
                                                        <Trash2 className="mr-2 h-4 w-4" /> Supprimer
                                                    </DropdownMenuItem>
                                                </DropdownMenuContent>
                                            </DropdownMenu>
                                        </TableCell>
                                    </TableRow>
                                ))
                            )}
                        </TableBody>
                    </Table>
                </div>
            </Card>

            {/* CREATE */}
            <Dialog open={openCreate} onOpenChange={setOpenCreate}>
                <DialogContent>
                    <DialogHeader>
                        <DialogTitle>Nouveau vol</DialogTitle>
                        <DialogDescription>Renseignez les informations du vol.</DialogDescription>
                    </DialogHeader>
                    <div className="grid gap-4 py-2">
                        <div className="grid grid-cols-1 gap-3 md:grid-cols-3">
                            <div className="space-y-2">
                                <Label>Numéro de vol</Label>
                                <Input
                                    value={createForm.numeroVol}
                                    onChange={(e) => setCreateForm((f) => ({ ...f, numeroVol: e.target.value }))}
                                    placeholder="AF1234"
                                />
                            </div>
                            <div className="space-y-2">
                                <Label>Origine</Label>
                                <Input
                                    value={createForm.origine}
                                    onChange={(e) => setCreateForm((f) => ({ ...f, origine: e.target.value }))}
                                    placeholder="ORY"
                                />
                            </div>
                            <div className="space-y-2">
                                <Label>Destination</Label>
                                <Input
                                    value={createForm.destination}
                                    onChange={(e) => setCreateForm((f) => ({ ...f, destination: e.target.value }))}
                                    placeholder="NCE"
                                />
                            </div>
                        </div>

                        <div className="grid grid-cols-1 gap-3 md:grid-cols-2">
                            <div className="space-y-2">
                                <Label>Heure de départ</Label>
                                <Input
                                    type="datetime-local"
                                    value={createForm.heureDepart}
                                    onChange={(e) =>
                                        setCreateForm((f) => ({ ...f, heureDepart: e.target.value }))
                                    }
                                />
                            </div>
                            <div className="space-y-2">
                                <Label>Heure d’arrivée</Label>
                                <Input
                                    type="datetime-local"
                                    value={createForm.heureArrivee}
                                    onChange={(e) =>
                                        setCreateForm((f) => ({ ...f, heureArrivee: e.target.value }))
                                    }
                                />
                            </div>
                        </div>
                    </div>
                    <DialogFooter className="gap-2">
                        <Button variant="outline" onClick={() => setOpenCreate(false)}>Annuler</Button>
                        <Button
                            onClick={async () => {
                                try {
                                    await createVol();
                                } catch (e: any) {
                                    alert(e?.message || "Erreur à la création");
                                }
                            }}
                        >
                            Enregistrer
                        </Button>
                    </DialogFooter>
                </DialogContent>
            </Dialog>

            {/* EDIT */}
            <Dialog open={!!openEdit} onOpenChange={(o) => !o && setOpenEdit(null)}>
                <DialogContent>
                    <DialogHeader>
                        <DialogTitle>Modifier le vol {openEdit?.numeroVol}</DialogTitle>
                    </DialogHeader>
                    <div className="grid gap-4 py-2">
                        <div className="grid grid-cols-1 gap-3 md:grid-cols-2">
                            <div className="space-y-2">
                                <Label>Origine</Label>
                                <Input
                                    value={editForm.origine ?? ""}
                                    onChange={(e) => setEditForm((f) => ({ ...f, origine: e.target.value }))}
                                />
                            </div>
                            <div className="space-y-2">
                                <Label>Destination</Label>
                                <Input
                                    value={editForm.destination ?? ""}
                                    onChange={(e) => setEditForm((f) => ({ ...f, destination: e.target.value }))}
                                />
                            </div>
                        </div>

                        <div className="grid grid-cols-1 gap-3 md:grid-cols-2">
                            <div className="space-y-2">
                                <Label>Heure de départ</Label>
                                <Input
                                    type="datetime-local"
                                    value={editForm.heureDepart ?? ""}
                                    onChange={(e) =>
                                        setEditForm((f) => ({ ...f, heureDepart: e.target.value }))
                                    }
                                />
                            </div>
                            <div className="space-y-2">
                                <Label>Heure d’arrivée</Label>
                                <Input
                                    type="datetime-local"
                                    value={editForm.heureArrivee ?? ""}
                                    onChange={(e) =>
                                        setEditForm((f) => ({ ...f, heureArrivee: e.target.value }))
                                    }
                                />
                            </div>
                        </div>

                        <div className="space-y-2">
                            <Label>État</Label>
                            <Select
                                value={editForm.etat ?? undefined}
                                onValueChange={(v) => setEditForm((f) => ({ ...f, etat: v }))}
                            >
                                <SelectTrigger>
                                    <SelectValue placeholder="Choisir un état" />
                                </SelectTrigger>
                                <SelectContent>
                                    {VOL_ETATS.map((e) => (
                                        <SelectItem key={e} value={e}>{e}</SelectItem>
                                    ))}
                                </SelectContent>
                            </Select>
                        </div>

                        <div className="space-y-2">
                            <Label>Avion (UUID — optionnel)</Label>
                            <Input
                                value={editForm.avionId ?? ""}
                                onChange={(e) =>
                                    setEditForm((f) => ({ ...f, avionId: e.target.value || null }))
                                }
                                placeholder="00000000-0000-0000-0000-000000000000"
                            />
                        </div>
                    </div>
                    <DialogFooter className="gap-2">
                        <Button variant="outline" onClick={() => setOpenEdit(null)}>
                            Annuler
                        </Button>
                        {openEdit && (
                            <Button
                                onClick={async () => {
                                    try {
                                        await updateVol(openEdit.id);
                                    } catch (e: any) {
                                        alert(e?.message || "Erreur mise à jour");
                                    }
                                }}
                            >
                                Enregistrer
                            </Button>
                        )}
                    </DialogFooter>
                </DialogContent>
            </Dialog>

            {/* CHANGE STATE */}
            <Dialog open={!!openEtat} onOpenChange={(o) => !o && setOpenEtat(null)}>
                <DialogContent>
                    <DialogHeader>
                        <DialogTitle>Changer l’état du vol</DialogTitle>
                    </DialogHeader>
                    <div className="space-y-2 py-2">
                        <Label>État</Label>
                        <Select value={etatForm} onValueChange={setEtatForm}>
                            <SelectTrigger>
                                <SelectValue />
                            </SelectTrigger>
                            <SelectContent>
                                {VOL_ETATS.map((e) => (
                                    <SelectItem key={e} value={e}>{e}</SelectItem>
                                ))}
                            </SelectContent>
                        </Select>
                    </div>
                    <DialogFooter className="gap-2">
                        <Button variant="outline" onClick={() => setOpenEtat(null)}>Annuler</Button>
                        <Button
                            onClick={async () => {
                                try {
                                    await changeEtat();
                                } catch (e: any) {
                                    alert(e?.message || "Erreur mise à jour état");
                                }
                            }}
                        >
                            Mettre à jour
                        </Button>
                    </DialogFooter>
                </DialogContent>
            </Dialog>

            {/* ASSIGN AVION */}
            <Dialog open={!!openAssign} onOpenChange={(o) => !o && setOpenAssign(null)}>
                <DialogContent>
                    <DialogHeader>
                        <DialogTitle>Assigner un avion</DialogTitle>
                        <DialogDescription>Entrez l’UUID de l’avion à associer.</DialogDescription>
                    </DialogHeader>
                    <div className="space-y-2 py-2">
                        <Label>AvionId (UUID)</Label>
                        <Input
                            value={assignForm}
                            onChange={(e) => setAssignForm(e.target.value)}
                            placeholder="00000000-0000-0000-0000-000000000000"
                        />
                    </div>
                    <DialogFooter className="gap-2">
                        <Button variant="outline" onClick={() => setOpenAssign(null)}>Annuler</Button>
                        <Button
                            onClick={async () => {
                                try {
                                    await assignAvion();
                                } catch (e: any) {
                                    alert(e?.message || "Erreur assignation");
                                }
                            }}
                        >
                            Assigner
                        </Button>
                    </DialogFooter>
                </DialogContent>
            </Dialog>

            {/* HISTORIC */}
            <Dialog open={!!openHistory} onOpenChange={(o) => !o && setOpenHistory(null)}>
                <DialogContent>
                    <DialogHeader>
                        <DialogTitle>Historique du vol</DialogTitle>
                        <DialogDescription>
                            Liste des changements d’état.
                        </DialogDescription>
                    </DialogHeader>

                    <div className="max-h-80 overflow-y-auto py-4">
                        {historyList.length === 0 ? (
                            <p className="text-sm text-muted-foreground">Aucun historique.</p>
                        ) : (
                            <div className="relative pl-4">
                                {/* Vertical line */}
                                <div className="absolute left-2 top-0 bottom-0 w-px bg-border" />

                                {/* Events */}
                                <div className="space-y-6">
                                    {historyList
                                        .sort(
                                            (a, b) => new Date(a.changedAt).getTime() -
                                                new Date(b.changedAt).getTime()
                                        )
                                        .map((h, i) => (
                                            <div key={i} className="relative pl-6">
                                                {/* Timeline dot */}
                                                <div
                                                    className={`absolute left-[-6px] top-[4px] h-3 w-3 rounded-full border-2 border-white shadow ${colorForEtat(
                                                        h.etat
                                                    )}`}
                                                ></div>

                                                {/* Content */}
                                                <div className="flex flex-col">
                                                    <span className="font-medium">{h.etat}</span>
                                                    <span className="text-xs text-muted-foreground">
                                    {fmt.format(new Date(h.changedAt))}
                                </span>
                                                </div>
                                            </div>
                                        ))}
                                </div>
                            </div>
                        )}
                    </div>

                    <DialogFooter>
                        <Button variant="outline" onClick={() => setOpenHistory(null)}>
                            Fermer
                        </Button>
                    </DialogFooter>
                </DialogContent>
            </Dialog>
        </div>
    );
}
